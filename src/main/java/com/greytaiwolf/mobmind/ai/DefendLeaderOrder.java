package com.greytaiwolf.mobmind.ai;

import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.hive.HiveData;
import com.greytaiwolf.mobmind.hive.HiveOrder;
import com.greytaiwolf.mobmind.hive.HiveRole;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class DefendLeaderOrder {
    public static final double COMMAND_RADIUS = 48.0D;
    private static final double GUARD_MOVE_SPEED = 1.35D;
    private static final double SUPPORT_MOVE_SPEED = 1.1D;

    private DefendLeaderOrder() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Player attacker) {
        AABB commandArea = leader.getBoundingBox().inflate(COMMAND_RADIUS);
        double maxDistanceSqr = COMMAND_RADIUS * COMMAND_RADIUS;

        for (Zombie zombie : level.getEntitiesOfClass(Zombie.class, commandArea, other -> isDefender(other, group, leader, maxDistanceSqr))) {
            MobMindData data = ModAttachments.get(zombie);
            data.setCurrentOrder(HiveOrder.DEFEND_LEADER.name());
            zombie.setTarget(attacker);
            double speed = data.getRole() == HiveRole.GUARD ? GUARD_MOVE_SPEED : SUPPORT_MOVE_SPEED;
            zombie.getNavigation().moveTo(attacker, speed);
        }
    }

    private static boolean isDefender(Zombie zombie, HiveData group, Zombie leader, double maxDistanceSqr) {
        if (zombie == leader || !zombie.isAlive() || zombie.isNoAi() || zombie.getType() != EntityType.ZOMBIE) {
            return false;
        }

        MobMindData data = ModAttachments.getExisting(zombie);
        return data != null
                && group.getGroupId().equals(data.getGroupId())
                && zombie.distanceToSqr(leader) <= maxDistanceSqr;
    }
}
