package com.example.mobmind.ai;

import com.example.mobmind.hive.HiveData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;

public final class GuardRoleExecutor {
    private static final double RETURN_DISTANCE_SQR = 144.0D;
    private static final double TOO_CLOSE_DISTANCE_SQR = 9.0D;
    private static final double MOVE_SPEED = 1.1D;

    private GuardRoleExecutor() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Zombie guard) {
        double distanceToLeader = guard.distanceToSqr(leader);
        if (distanceToLeader > RETURN_DISTANCE_SQR) {
            guard.getNavigation().moveTo(leader, MOVE_SPEED);
        } else if (distanceToLeader < TOO_CLOSE_DISTANCE_SQR && guard.getTarget() == null) {
            guard.getNavigation().stop();
        }
    }
}
