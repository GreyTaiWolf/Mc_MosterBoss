package com.example.mobmind.hive;

import com.example.mobmind.attachment.MobMindData;
import com.example.mobmind.attachment.ModAttachments;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import com.example.mobmind.species.SpeciesProfiles;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class HiveManager {
    private static final Map<ResourceKey<Level>, Map<UUID, HiveData>> GROUPS_BY_LEVEL = new HashMap<>();

    private HiveManager() {
    }

    public static HiveData getOrCreateGroup(ServerLevel level, UUID groupId, UUID leaderUuid, HiveOrder order) {
        Map<UUID, HiveData> groups = groups(level);
        HiveData group = groups.get(groupId);
        if (group == null) {
            group = new HiveData(groupId, leaderUuid, order);
            groups.put(groupId, group);
        } else {
            group.setLeaderUuid(leaderUuid);
            group.setCurrentOrder(order);
        }
        return group;
    }

    public static Optional<HiveData> getGroup(ServerLevel level, @Nullable UUID groupId) {
        if (groupId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(groups(level).get(groupId));
    }

    public static Collection<HiveData> getGroups(ServerLevel level) {
        return groups(level).values();
    }

    public static boolean hasActiveLeaderNearby(ServerLevel level, ResourceLocation speciesId, ChunkPos center, int chunkRadius) {
        Map<UUID, HiveData> groups = groups(level);
        java.util.List<UUID> staleGroupIds = new java.util.ArrayList<>();
        boolean foundActiveNearby = false;
        for (Map.Entry<UUID, HiveData> entry : groups.entrySet()) {
            UUID groupId = entry.getKey();
            HiveData group = entry.getValue();
            if (!speciesId.equals(group.getSpeciesId())) {
                continue;
            }

            Entity entity = level.getEntity(group.getLeaderUuid());
            if (!(entity instanceof Zombie leader) || !leader.isAlive()) {
                staleGroupIds.add(groupId);
                continue;
            }

            MobMindData data = ModAttachments.getExisting(leader);
            if (data == null || !data.isLeader() || !groupId.equals(data.getGroupId()) || !SpeciesProfiles.matchesSpecies(leader, speciesId)) {
                staleGroupIds.add(groupId);
                continue;
            }

            ChunkPos leaderChunk = leader.chunkPosition();
            int dx = Math.abs(leaderChunk.x - center.x);
            int dz = Math.abs(leaderChunk.z - center.z);
            if (Math.max(dx, dz) <= chunkRadius) {
                foundActiveNearby = true;
            }
        }

        for (UUID staleGroupId : staleGroupIds) {
            disbandGroup(level, staleGroupId);
        }
        return foundActiveNearby;
    }

    public static void setGroupTarget(ServerLevel level, UUID groupId, UUID targetUuid) {
        setGroupTarget(level, groupId, targetUuid, HiveOrder.HUNT_PLAYER);
    }

    public static void setGroupTarget(ServerLevel level, UUID groupId, UUID targetUuid, HiveOrder order) {
        HiveData group = groups(level).get(groupId);
        if (group == null) {
            return;
        }

        group.setTargetUuid(targetUuid);
        setGroupOrder(level, groupId, order);
    }


    public static void reportThreat(ServerLevel level, UUID groupId, UUID playerUuid, long gameTime, int levelValue) {
        HiveData group = groups(level).get(groupId);
        if (group == null) {
            return;
        }

        group.setPendingThreatUuid(playerUuid);
        group.setLastThreatReportTick(gameTime);
        group.setThreatLevel(levelValue);
    }

    public static void clearGroupTarget(ServerLevel level, UUID groupId, HiveOrder nextOrder) {
        HiveData group = groups(level).get(groupId);
        if (group == null) {
            return;
        }

        group.setTargetUuid(null);
        setGroupOrder(level, groupId, nextOrder);
    }

    public static void setGroupOrder(ServerLevel level, UUID groupId, HiveOrder order) {
        HiveData group = groups(level).get(groupId);
        if (group == null) {
            return;
        }

        group.setCurrentOrder(order);
        forEachGroupZombie(level, groupId, zombie -> ModAttachments.get(zombie).setCurrentOrder(order.name()));
    }

    public static void disbandGroup(ServerLevel level, UUID groupId) {
        disbandGroup(level, groupId, null);
    }

    public static void disbandGroup(ServerLevel level, UUID groupId, @Nullable BlockPos dropPos) {
        groups(level).remove(groupId);
        if (dropPos != null) {
            HiveSavedData.get(level).dropAndRemoveGroup(level, groupId, dropPos);
        } else {
            HiveSavedData.get(level).removeGroup(groupId);
        }
        forEachGroupZombie(level, groupId, zombie -> {
            HiveSelector.restoreMobMindState(zombie);
            zombie.setTarget(null);
            zombie.setLastHurtByMob(null);
            zombie.getNavigation().stop();
            ModAttachments.get(zombie).clearGroup();
        });
    }

    public static void forEachGroupZombie(ServerLevel level, UUID groupId, ZombieConsumer consumer) {
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Zombie zombie) || zombie.getType() != EntityType.ZOMBIE) {
                continue;
            }
            if (!zombie.isAlive()) {
                continue;
            }

            MobMindData data = ModAttachments.getExisting(zombie);
            if (data != null && groupId.equals(data.getGroupId())) {
                consumer.accept(zombie);
            }
        }
    }

    private static Map<UUID, HiveData> groups(ServerLevel level) {
        return GROUPS_BY_LEVEL.computeIfAbsent(level.dimension(), ignored -> new HashMap<>());
    }

    @FunctionalInterface
    public interface ZombieConsumer {
        void accept(Zombie zombie);
    }
}
