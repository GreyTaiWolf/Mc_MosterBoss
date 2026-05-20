package com.greytaiwolf.mobmind.ai;

import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.hive.HiveData;
import com.greytaiwolf.mobmind.hive.HiveManager;
import com.greytaiwolf.mobmind.hive.HiveOrder;
import com.greytaiwolf.mobmind.hive.HiveSelector;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import com.greytaiwolf.mobmind.species.SpeciesProfiles;

public final class OrderExecutor {
    public static final int EXECUTE_INTERVAL_TICKS = 20;
    private static final float RETREAT_HEALTH_RATIO = 0.30F;
    private static final long THREAT_REPORT_TTL_TICKS = 80L;

    private OrderExecutor() {
    }

    public static void tick(ServerLevel level) {
        for (HiveData group : new ArrayList<>(HiveManager.getGroups(level))) {
            Zombie leader = getLeader(level, group);
            if (leader == null) {
                HiveManager.disbandGroup(level, group.getGroupId());
                continue;
            }

            MobMindData leaderData = ModAttachments.get(leader);
            if (!leaderData.isLeader() || !group.getGroupId().equals(leaderData.getGroupId())) {
                HiveManager.disbandGroup(level, group.getGroupId());
                continue;
            }
            HiveSelector.applyLeaderHighlight(leader);

            if (shouldRetreat(leader) || group.getCurrentOrder() == HiveOrder.RETREAT) {
                HiveManager.clearGroupTarget(level, group.getGroupId(), HiveOrder.RETREAT);
                RetreatOrder.execute(level, group, leader);
                continue;
            }

            if (group.getCurrentOrder() == HiveOrder.DEFEND_LEADER) {
                Player target = getTarget(level, group);
                if (target == null || !target.isAlive()) {
                    HiveManager.clearGroupTarget(level, group.getGroupId(), HiveOrder.IDLE);
                    continue;
                }

                DefendLeaderOrder.execute(level, group, leader, target);
                continue;
            }

            if (SeekShadeOrder.needsShade(level, group, leader)) {
                HiveManager.setGroupOrder(level, group.getGroupId(), HiveOrder.SEEK_SHADE);
                SeekShadeOrder.execute(level, group, leader);
                continue;
            }

            if (group.getCurrentOrder() == HiveOrder.SEEK_SHADE) {
                HiveManager.setGroupOrder(level, group.getGroupId(), HiveOrder.BUILD_SHELTER);
            }

            Player visibleTarget = findVisiblePlayer(level, leader);
            if (visibleTarget != null && group.getCurrentOrder() != HiveOrder.RETREAT) {
                HiveManager.reportThreat(level, group.getGroupId(), visibleTarget.getUUID(), level.getGameTime(), 1);
            }

            consumeThreatReport(level, group);

            if (group.getCurrentOrder() == HiveOrder.HUNT_PLAYER) {
                Player target = getTarget(level, group);
                if (target == null || !target.isAlive()) {
                    HiveManager.clearGroupTarget(level, group.getGroupId(), HiveOrder.IDLE);
                    continue;
                }

                HuntPlayerOrder.execute(level, group, leader, target);
                continue;
            }

            if (group.getCurrentOrder() == HiveOrder.IDLE || group.getCurrentOrder() == HiveOrder.BUILD_SHELTER) {
                RoleExecutor.executeIdleRoles(level, group, leader);
            }
        }
    }


    private static void consumeThreatReport(ServerLevel level, HiveData group) {
        if (group.getPendingThreatUuid() == null) {
            return;
        }

        long age = level.getGameTime() - group.getLastThreatReportTick();
        if (age > THREAT_REPORT_TTL_TICKS) {
            group.clearThreatReport();
            return;
        }

        if (group.getCurrentOrder() == HiveOrder.RETREAT) {
            return;
        }

        HiveManager.setGroupTarget(level, group.getGroupId(), group.getPendingThreatUuid(), HiveOrder.HUNT_PLAYER);
        group.clearThreatReport();
    }

    private static boolean shouldRetreat(Zombie leader) {
        return leader.getHealth() / leader.getMaxHealth() < RETREAT_HEALTH_RATIO;
    }

    @Nullable
    private static Zombie getLeader(ServerLevel level, HiveData group) {
        Entity entity = level.getEntity(group.getLeaderUuid());
        if (entity instanceof Zombie zombie && zombie.isAlive() && SpeciesProfiles.forMonster(zombie) != null) {
            return zombie;
        }

        return null;
    }

    @Nullable
    private static Player getTarget(ServerLevel level, HiveData group) {
        if (group.getTargetUuid() == null) {
            return null;
        }

        Entity entity = level.getEntity(group.getTargetUuid());
        return entity instanceof Player player ? player : null;
    }

    @Nullable
    private static ServerPlayer findVisiblePlayer(ServerLevel level, Zombie leader) {
        double maxDistanceSqr = HuntPlayerOrder.COMMAND_RADIUS * HuntPlayerOrder.COMMAND_RADIUS;
        return level.players().stream()
                .filter(ServerPlayer::isAlive)
                .filter(player -> !player.isSpectator() && !player.isCreative())
                .filter(player -> leader.canAttack(player))
                .filter(player -> leader.distanceToSqr(player) <= maxDistanceSqr)
                .filter(leader::hasLineOfSight)
                .min(Comparator.comparingDouble(leader::distanceToSqr))
                .orElse(null);
    }
}
