package com.greytaiwolf.mobmind.hive;

import com.greytaiwolf.mobmind.MobMindMod;
import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.config.MobMindConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.greytaiwolf.mobmind.species.SpeciesProfile;
import com.greytaiwolf.mobmind.species.SpeciesProfiles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;

public final class HiveSelector {
    public static final int SCAN_INTERVAL_TICKS = 200;
    public static final int LEADER_AWARENESS_THRESHOLD = 100;
    public static final int LEADER_LEVEL_ONE = 1;
    public static final int LEVEL_ONE_CAPACITY = 6;
    public static final int LEVEL_ONE_MEMBER_COUNT = LEVEL_ONE_CAPACITY - 1;
    public static final int MIN_CANDIDATES = LEVEL_ONE_CAPACITY;
    public static final double GROUP_RADIUS = 32.0D;
    private static final int LEADER_GLOW_DURATION_TICKS = 240;

    private static final Component LEADER_NAME = Component.literal("\u89c9\u9192\u50f5\u5c38\u9996\u9886");
    private static final ResourceLocation LEADER_HEALTH_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_health");
    private static final ResourceLocation LEADER_ATTACK_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_attack");
    private static final ResourceLocation LEADER_SCALE_MODIFIER =
            ResourceLocation.fromNamespaceAndPath(MobMindMod.MODID, "awakened_zombie_leader_scale");
    private static final float VANILLA_EQUIPMENT_DROP_CHANCE = 0.085F;

    private HiveSelector() {
    }

    public static void scan(ServerLevel level) {
        List<Zombie> candidates = new ArrayList<>();
        Set<UUID> alreadyGrouped = new HashSet<>();

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Zombie zombie) || !isBaseZombie(zombie)) {
                continue;
            }

            MobMindData data = ModAttachments.getExisting(zombie);
            if (data != null && data.isLeader() && data.getGroupId() != null) {
                ensureLeaderState(level, zombie, data);
                alreadyGrouped.add(zombie.getUUID());
                continue;
            }

            if (isCandidate(zombie, data)) {
                candidates.add(zombie);
            } else if (data != null && data.getGroupId() != null) {
                alreadyGrouped.add(zombie.getUUID());
            }
        }

        Set<UUID> assigned = new HashSet<>(alreadyGrouped);
        double radiusSqr = GROUP_RADIUS * GROUP_RADIUS;
        for (Zombie center : candidates) {
            if (assigned.contains(center.getUUID())) {
                continue;
            }

            List<Zombie> nearby = candidates.stream()
                    .filter(zombie -> !assigned.contains(zombie.getUUID()))
                    .filter(zombie -> zombie.distanceToSqr(center) <= radiusSqr)
                    .toList();
            if (nearby.size() < MIN_CANDIDATES) {
                continue;
            }

            Zombie leader = nearby.stream()
                    .max(Comparator.comparingInt(zombie -> ModAttachments.get(zombie).getAwareness()))
                    .orElse(center);
            List<Zombie> team = selectLevelOneTeam(leader, nearby);
            if (team.size() < LEVEL_ONE_CAPACITY) {
                continue;
            }

            createGroup(level, leader, team);
            team.forEach(zombie -> assigned.add(zombie.getUUID()));
        }
    }

    private static boolean isCandidate(Zombie zombie, MobMindData data) {
        return zombie.isAlive()
                && !zombie.isNoAi()
                && data != null
                && data.getGroupId() == null
                && !data.isLeader()
                && data.getAwareness() >= LEADER_AWARENESS_THRESHOLD;
    }

    private static boolean isBaseZombie(Zombie zombie) {
        return zombie.getType() == EntityType.ZOMBIE;
    }

    private static void createGroup(ServerLevel level, Zombie leader, List<Zombie> team) {
        SpeciesProfile profile = SpeciesProfiles.forMonster(leader);
        if (profile == null) {
            return;
        }

        ResourceLocation speciesId = profile.id();
        ChunkPos centerChunk = leader.chunkPosition();
        int exclusionRadius = MobMindConfig.SAME_SPECIES_LEADER_EXCLUSION_CHUNK_RADIUS.get();
        if (HiveManager.hasActiveLeaderNearby(level, speciesId, centerChunk, exclusionRadius)) {
            return;
        }

        UUID groupId = UUID.randomUUID();
        UUID leaderUuid = leader.getUUID();
        long assignedAt = level.getGameTime();
        List<Zombie> roleMembers = team.stream()
                .filter(member -> member != leader)
                .sorted(memberPriority(leader))
                .limit(LEVEL_ONE_MEMBER_COUNT)
                .toList();

        assignMember(leader, groupId, leaderUuid, true, HiveRole.LEADER, assignedAt);
        assignRoleIfPresent(roleMembers, 0, groupId, leaderUuid, HiveRole.GUARD, assignedAt);
        assignRoleIfPresent(roleMembers, 1, groupId, leaderUuid, HiveRole.GUARD, assignedAt);
        assignRoleIfPresent(roleMembers, 2, groupId, leaderUuid, HiveRole.MINER, assignedAt);
        assignRoleIfPresent(roleMembers, 3, groupId, leaderUuid, HiveRole.PATROL, assignedAt);
        assignRoleIfPresent(roleMembers, 4, groupId, leaderUuid, HiveRole.BUILDER, assignedAt);

        HiveData group = HiveManager.getOrCreateGroup(level, groupId, leaderUuid, HiveOrder.IDLE);
        group.setSpeciesId(speciesId);
        group.setLeaderLevel(LEADER_LEVEL_ONE);
        group.setCapacity(LEVEL_ONE_CAPACITY);
        applyLeaderPresentation(leader);
        MobMindMod.LOGGER.debug("Created level {} zombie hive {} with leader {} and {} total zombies",
                LEADER_LEVEL_ONE, groupId, leaderUuid, team.size());
    }

    private static void ensureLeaderState(ServerLevel level, Zombie leader, MobMindData data) {
        data.setLeaderUuid(leader.getUUID());
        data.setLeaderLevel(LEADER_LEVEL_ONE);
        data.setRole(HiveRole.LEADER);
        HiveData group = HiveManager.getOrCreateGroup(level, data.getGroupId(), leader.getUUID(), HiveOrder.fromName(data.getCurrentOrder()));
        group.setLeaderLevel(LEADER_LEVEL_ONE);
        group.setCapacity(LEVEL_ONE_CAPACITY);
        applyLeaderPresentation(leader);
    }

    private static List<Zombie> selectLevelOneTeam(Zombie leader, List<Zombie> nearby) {
        List<Zombie> members = nearby.stream()
                .filter(zombie -> zombie != leader)
                .sorted(memberPriority(leader))
                .limit(LEVEL_ONE_MEMBER_COUNT)
                .toList();
        List<Zombie> team = new ArrayList<>(LEVEL_ONE_CAPACITY);
        team.add(leader);
        team.addAll(members);
        return team;
    }

    private static Comparator<Zombie> memberPriority(Zombie leader) {
        return Comparator.<Zombie>comparingInt(zombie -> ModAttachments.get(zombie).getAwareness())
                .reversed()
                .thenComparingDouble(zombie -> zombie.distanceToSqr(leader));
    }

    private static void assignRoleIfPresent(List<Zombie> members, int index, UUID groupId, UUID leaderUuid, HiveRole role, long assignedAt) {
        if (index < members.size()) {
            assignMember(members.get(index), groupId, leaderUuid, false, role, assignedAt);
            applyRoleEquipment(members.get(index), role);
        }
    }

    private static void assignMember(Zombie zombie, UUID groupId, UUID leaderUuid, boolean leader, HiveRole role, long assignedAt) {
        MobMindData data = ModAttachments.get(zombie);
        data.setGroupId(groupId);
        data.setLeaderUuid(leaderUuid);
        data.setLeader(leader);
        data.setLeaderLevel(LEADER_LEVEL_ONE);
        data.setRole(role);
        data.setRoleAssignedAt(assignedAt);
        data.setCurrentOrder(HiveOrder.IDLE.name());
    }

    private static void applyRoleEquipment(Zombie zombie, HiveRole role) {
        captureEquipmentSnapshotIfNeeded(zombie);
        switch (role) {
            case GUARD -> {
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
            }
            case MINER -> {
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
                zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            }
            case PATROL -> {
                zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
            }
            case BUILDER -> {
                zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
                zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.COBBLESTONE));
                zombie.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                zombie.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
            }
            default -> {
            }
        }
    }

    public static void applyLeaderPresentation(Zombie leader) {
        captureEquipmentSnapshotIfNeeded(leader);
        leader.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        leader.setDropChance(EquipmentSlot.HEAD, 0.0F);
        applyLeaderHighlight(leader);
        leader.setCustomName(LEADER_NAME);
        leader.setCustomNameVisible(true);
        addLeaderAttribute(leader.getAttribute(Attributes.MAX_HEALTH), LEADER_HEALTH_MODIFIER, 0.20D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addLeaderAttribute(leader.getAttribute(Attributes.ATTACK_DAMAGE), LEADER_ATTACK_MODIFIER, 1.0D, AttributeModifier.Operation.ADD_VALUE);
        addLeaderAttribute(leader.getAttribute(Attributes.SCALE), LEADER_SCALE_MODIFIER, 0.35D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        leader.refreshDimensions();
        leader.setHealth(Math.min(leader.getMaxHealth(), leader.getHealth() + 4.0F));
    }

    public static void restoreMobMindState(Zombie zombie) {
        MobMindData data = ModAttachments.get(zombie);
        if (data.isEquipmentSnapshotCaptured()) {
            zombie.setItemSlot(EquipmentSlot.MAINHAND, data.getOriginalMainHand());
            zombie.setItemSlot(EquipmentSlot.OFFHAND, data.getOriginalOffHand());
            zombie.setItemSlot(EquipmentSlot.HEAD, data.getOriginalHead());
            zombie.setDropChance(EquipmentSlot.MAINHAND, data.getOriginalMainHandDropChance());
            zombie.setDropChance(EquipmentSlot.OFFHAND, data.getOriginalOffHandDropChance());
            zombie.setDropChance(EquipmentSlot.HEAD, data.getOriginalHeadDropChance());
            data.clearEquipmentSnapshot();
        }

        if (data.isLeader()) {
            removeLeaderAttribute(zombie.getAttribute(Attributes.MAX_HEALTH), LEADER_HEALTH_MODIFIER);
            removeLeaderAttribute(zombie.getAttribute(Attributes.ATTACK_DAMAGE), LEADER_ATTACK_MODIFIER);
            removeLeaderAttribute(zombie.getAttribute(Attributes.SCALE), LEADER_SCALE_MODIFIER);
            zombie.refreshDimensions();
            zombie.setHealth(Math.min(zombie.getHealth(), zombie.getMaxHealth()));
            zombie.setGlowingTag(false);
            zombie.setCustomNameVisible(false);
        }
    }

    private static void captureEquipmentSnapshotIfNeeded(Zombie zombie) {
        MobMindData data = ModAttachments.get(zombie);
        if (data.isEquipmentSnapshotCaptured()) {
            return;
        }

        data.setOriginalMainHand(zombie.getItemBySlot(EquipmentSlot.MAINHAND));
        data.setOriginalOffHand(zombie.getItemBySlot(EquipmentSlot.OFFHAND));
        data.setOriginalHead(zombie.getItemBySlot(EquipmentSlot.HEAD));
        data.setOriginalMainHandDropChance(VANILLA_EQUIPMENT_DROP_CHANCE);
        data.setOriginalOffHandDropChance(VANILLA_EQUIPMENT_DROP_CHANCE);
        data.setOriginalHeadDropChance(VANILLA_EQUIPMENT_DROP_CHANCE);
        data.setEquipmentSnapshotCaptured(true);
    }

    public static void applyLeaderHighlight(Zombie leader) {
        leader.setGlowingTag(true);
        leader.addEffect(new MobEffectInstance(MobEffects.GLOWING, LEADER_GLOW_DURATION_TICKS, 0, true, false, false));
    }

    private static void addLeaderAttribute(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }

        attribute.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeLeaderAttribute(AttributeInstance attribute, ResourceLocation id) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(id);
    }
}
