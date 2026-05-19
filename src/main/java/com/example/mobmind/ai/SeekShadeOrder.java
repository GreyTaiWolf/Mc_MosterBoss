package com.example.mobmind.ai;

import com.example.mobmind.hive.HiveData;
import com.example.mobmind.hive.HiveManager;
import com.example.mobmind.hive.HiveSavedData;
import com.example.mobmind.hive.ShelterPlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;

public final class SeekShadeOrder {
    private static final double MOVE_SPEED = 1.15D;
    private static final double CLOSE_DISTANCE_SQR = 9.0D;

    private SeekShadeOrder() {
    }

    public static boolean needsShade(ServerLevel level, HiveData group, Zombie leader) {
        if (!level.isDay()) {
            return false;
        }
        if (ShelterPlanner.isSunExposed(level, leader)) {
            return true;
        }

        final boolean[] exposed = {false};
        HiveManager.forEachGroupZombie(level, group.getGroupId(), zombie -> {
            if (ShelterPlanner.isSunExposed(level, zombie)) {
                exposed[0] = true;
            }
        });
        return exposed[0];
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader) {
        HiveSavedData savedData = HiveSavedData.get(level);
        HiveSavedData.GroupStorage storage = savedData.getOrCreate(group.getGroupId());
        BlockPos anchor = storage.getShelterAnchor();
        if (anchor == null || !ShelterPlanner.isSafeStand(level, anchor)) {
            anchor = ShelterPlanner.findShadeAnchor(level, leader);
            storage.setShelterAnchor(anchor);
            savedData.setDirty();
        }

        BlockPos target = anchor;
        HiveManager.forEachGroupZombie(level, group.getGroupId(), zombie -> {
            double targetX = target.getX() + 0.5D;
            double targetY = target.getY();
            double targetZ = target.getZ() + 0.5D;
            if (zombie.distanceToSqr(targetX, targetY, targetZ) > CLOSE_DISTANCE_SQR) {
                zombie.getNavigation().moveTo(targetX, targetY, targetZ, MOVE_SPEED);
            } else {
                zombie.getNavigation().stop();
            }
        });
    }
}
