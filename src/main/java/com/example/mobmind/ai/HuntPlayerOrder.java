package com.example.mobmind.ai;

import com.example.mobmind.attachment.MobMindData;
import com.example.mobmind.attachment.ModAttachments;
import com.example.mobmind.hive.HiveData;
import com.example.mobmind.hive.HiveOrder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class HuntPlayerOrder {
    public static final double COMMAND_RADIUS = 48.0D;
    private static final double MOVE_SPEED = 1.15D;

    private HuntPlayerOrder() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Player target) {
        AABB commandArea = leader.getBoundingBox().inflate(COMMAND_RADIUS);
        double maxDistanceSqr = COMMAND_RADIUS * COMMAND_RADIUS;

        for (Zombie zombie : level.getEntitiesOfClass(Zombie.class, commandArea, other -> isSameGroup(other, group, leader, maxDistanceSqr))) {
            MobMindData data = ModAttachments.get(zombie);
            data.setCurrentOrder(HiveOrder.HUNT_PLAYER.name());
            zombie.setTarget(target);
            zombie.getNavigation().moveTo(target, MOVE_SPEED);
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
