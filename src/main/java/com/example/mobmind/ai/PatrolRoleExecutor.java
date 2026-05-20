package com.example.mobmind.ai;

import com.example.mobmind.hive.HiveData;
import com.example.mobmind.hive.HiveManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Nullable;

public final class PatrolRoleExecutor {
    private static final double PLAYER_SCAN_RADIUS = 40.0D;
    private static final double MAX_PATROL_DISTANCE_SQR = 50.0D * 50.0D;
    private static final double MIN_PATROL_RADIUS = 24.0D;
    private static final double PATROL_RADIUS_SPREAD = 16.0D;
    private static final double MOVE_SPEED = 1.0D;

    private PatrolRoleExecutor() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Zombie patrol) {
        ServerPlayer player = findVisiblePlayer(level, patrol);
        if (player != null) {
            HiveManager.reportThreat(level, group.getGroupId(), player.getUUID(), level.getGameTime(), 1);
            patrol.setTarget(player);
            patrol.getNavigation().moveTo(player, 1.15D);
            return;
        }

        if (patrol.distanceToSqr(leader) > MAX_PATROL_DISTANCE_SQR) {
            patrol.getNavigation().moveTo(leader, MOVE_SPEED);
            return;
        }

        if (patrol.tickCount % 100 == 0 || patrol.getNavigation().isDone()) {
            double angle = patrol.getRandom().nextDouble() * Math.PI * 2.0D;
            double radius = MIN_PATROL_RADIUS + patrol.getRandom().nextDouble() * PATROL_RADIUS_SPREAD;
            double targetX = leader.getX() + Math.cos(angle) * radius;
            double targetZ = leader.getZ() + Math.sin(angle) * radius;
            patrol.getNavigation().moveTo(targetX, leader.getY(), targetZ, MOVE_SPEED);
        }
    }

    @Nullable
    private static ServerPlayer findVisiblePlayer(ServerLevel level, Zombie patrol) {
        double maxDistanceSqr = PLAYER_SCAN_RADIUS * PLAYER_SCAN_RADIUS;
        return level.players().stream()
                .filter(ServerPlayer::isAlive)
                .filter(player -> !player.isSpectator() && !player.isCreative())
                .filter(player -> patrol.canAttack(player))
                .filter(player -> patrol.distanceToSqr(player) <= maxDistanceSqr)
                .filter(patrol::hasLineOfSight)
                .findFirst()
                .orElse(null);
    }
}
