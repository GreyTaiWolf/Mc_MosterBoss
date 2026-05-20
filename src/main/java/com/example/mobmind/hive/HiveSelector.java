package com.example.mobmind.hive;

import com.example.mobmind.MobMindMod;
import com.example.mobmind.attachment.MobMindData;
import com.example.mobmind.attachment.ModAttachments;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import com.example.mobmind.species.SpeciesProfile;
import com.example.mobmind.species.SpeciesProfiles;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HiveSelector {
    public static final int SCAN_INTERVAL_TICKS = 200;
    public static final int LEADER_AWARENESS_THRESHOLD = 100;
    public static final int LEADER_LEVEL_ONE = 1;
    public static final int LEVEL_ONE_CAPACITY = 6;

    private HiveSelector() {
    }

    public static void scan(ServerLevel level) {
        SpeciesProfile profile = SpeciesProfiles.zombie();
        List<Zombie> candidates = new ArrayList<>();
        Set<UUID> alreadyGrouped = new HashSet<>();

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Zombie zombie) || !profile.isEligibleLeader(zombie)) {
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
        double radiusSqr = profile.groupRadius() * profile.groupRadius();
        for (Zombie center : candidates) {
            if (assigned.contains(center.getUUID())) {
                continue;
            }

            List<Zombie> nearby = candidates.stream()
                    .filter(zombie -> !assigned.contains(zombie.getUUID()))
                    .filter(zombie -> zombie.distanceToSqr(center) <= radiusSqr)
                    .toList();
            if (nearby.size() < profile.groupCapacity()) {
                continue;
            }

            Zombie leader = nearby.stream()
                    .max(Comparator.comparingInt(zombie -> ModAttachments.get(zombie).getAwareness()))
                    .orElse(center);
            List<Zombie> team = selectLevelOneTeam(leader, nearby, profile);
            if (team.size() < profile.groupCapacity()) {
                continue;
            }

            createGroup(level, leader, team, profile);
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

    private static void createGroup(ServerLevel level, Zombie leader, List<Zombie> team, SpeciesProfile profile) {
        UUID groupId = UUID.randomUUID();
        UUID leaderUuid = leader.getUUID();
        long assignedAt = level.getGameTime();
        List<Zombie> roleMembers = team.stream()
                .filter(member -> member != leader)
                .sorted(memberPriority(leader))
                .limit(profile.roleTemplate().size())
                .toList();

        assignMember(leader, groupId, leaderUuid, true, HiveRole.LEADER, assignedAt);
        for (int i = 0; i < profile.roleTemplate().size(); i++) {
            assignRoleIfPresent(roleMembers, i, groupId, leaderUuid, profile.roleTemplate().get(i), assignedAt);
        }

        HiveData group = HiveManager.getOrCreateGroup(level, groupId, leaderUuid, HiveOrder.IDLE);
        group.setLeaderLevel(LEADER_LEVEL_ONE);
        group.setCapacity(profile.groupCapacity());
        profile.applyLeaderPresentation(leader);
        MobMindMod.LOGGER.debug("Created level {} zombie hive {} with leader {} and {} total zombies",
                LEADER_LEVEL_ONE, groupId, leaderUuid, team.size());
    }

    private static void ensureLeaderState(ServerLevel level, Zombie leader, MobMindData data) {
        SpeciesProfile profile = SpeciesProfiles.zombie();
        data.setLeaderUuid(leader.getUUID());
        data.setLeaderLevel(LEADER_LEVEL_ONE);
        data.setRole(HiveRole.LEADER);
        HiveData group = HiveManager.getOrCreateGroup(level, data.getGroupId(), leader.getUUID(), HiveOrder.fromName(data.getCurrentOrder()));
        group.setLeaderLevel(LEADER_LEVEL_ONE);
        group.setCapacity(profile.groupCapacity());
        profile.applyLeaderPresentation(leader);
    }

    private static List<Zombie> selectLevelOneTeam(Zombie leader, List<Zombie> nearby, SpeciesProfile profile) {
        List<Zombie> members = nearby.stream()
                .filter(zombie -> zombie != leader)
                .sorted(memberPriority(leader))
                .limit(profile.roleTemplate().size())
                .toList();
        List<Zombie> team = new ArrayList<>(profile.groupCapacity());
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

}