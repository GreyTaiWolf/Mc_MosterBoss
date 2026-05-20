package com.greytaiwolf.mobmind.ai;

import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.hive.HiveData;
import com.greytaiwolf.mobmind.hive.HiveOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;

public final class RetreatOrder {
    public static final double COMMAND_RADIUS = 48.0D;
    private static final double MOVE_SPEED = 1.2D;
    private static final double CLOSE_ENOUGH_DISTANCE_SQR = 9.0D;

    private RetreatOrder() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader) {
        MobMindData leaderData = ModAttachments.get(leader);
        BlockPos retreatPos = leaderData.getNestPos() != null ? leaderData.getNestPos() : leader.blockPosition();
        AABB commandArea = leader.getBoundingBox().inflate(COMMAND_RADIUS);
        double maxDistanceSqr = COMMAND_RADIUS * COMMAND_RADIUS;

        for (Zombie zombie : level.getEntitiesOfClass(Zombie.class, commandArea, other -> isSameGroup(other, group, leader, maxDistanceSqr))) {
            MobMindData data = ModAttachments.get(zombie);
            data.setCurrentOrder(HiveOrder.RETREAT.name());
            zombie.setTarget(null);

            double targetX = retreatPos.getX() + 0.5D;
            double targetY = retreatPos.getY();
            double targetZ = retreatPos.getZ() + 0.5D;
            if (zombie.distanceToSqr(targetX, targetY, targetZ) > CLOSE_ENOUGH_DISTANCE_SQR) {
                zombie.getNavigation().moveTo(targetX, targetY, targetZ, MOVE_SPEED);
            } else {
                zombie.getNavigation().stop();
            }
        }
    }

    private static boolean isSameGroup(Zombie zombie, HiveData group, Zombie leader, double maxDistanceSqr) {
        if (!zombie.isAlive() || zombie.isNoAi() || zombie.getType() != EntityType.ZOMBIE) {
            return false;
        }

        MobMindData data = ModAttachments.getExisting(zombie);
        return data != null
                && group.getGroupId().equals(data.getGroupId())
                && zombie.distanceToSqr(leader) <= maxDistanceSqr;
    }
}
