package com.greytaiwolf.mobmind.ai;

import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.hive.HiveData;
import com.greytaiwolf.mobmind.hive.HiveManager;
import com.greytaiwolf.mobmind.hive.HiveRole;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

public final class RoleExecutor {
    private RoleExecutor() {
    }

    public static void executeIdleRoles(ServerLevel level, HiveData group, Zombie leader) {
        HiveManager.forEachGroupZombie(level, group.getGroupId(), zombie -> {
            if (zombie == leader || zombie.getType() != EntityType.ZOMBIE) {
                return;
            }

            MobMindData data = ModAttachments.getExisting(zombie);
            HiveRole role = data == null ? HiveRole.NONE : data.getRole();
            switch (role) {
                case GUARD -> GuardRoleExecutor.execute(level, group, leader, zombie);
                case MINER -> MinerRoleExecutor.execute(level, group, leader, zombie);
                case PATROL -> PatrolRoleExecutor.execute(level, group, leader, zombie);
                case BUILDER -> BuilderRoleExecutor.execute(level, group, leader, zombie);
                default -> {
                }
            }
        });
    }
}
