package com.greytaiwolf.mobmind.ai;

import com.greytaiwolf.mobmind.config.MobMindConfig;
import com.greytaiwolf.mobmind.hive.HiveData;
import com.greytaiwolf.mobmind.hive.HiveSavedData;
import com.greytaiwolf.mobmind.hive.ShelterPlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class BuilderRoleExecutor {
    private static final double WORK_DISTANCE_SQR = 20.0D;
    private static final double MOVE_SPEED = 1.0D;

    private BuilderRoleExecutor() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Zombie builder) {
        HiveSavedData savedData = HiveSavedData.get(level);
        HiveSavedData.GroupStorage storage = savedData.getOrCreate(group.getGroupId());
        BlockPos anchor = storage.getShelterAnchor();
        if (anchor == null) {
            anchor = ShelterPlanner.findShadeAnchor(level, leader);
            storage.setShelterAnchor(anchor);
            savedData.setDirty();
        }

        BlockPos target = ShelterPlanner.findNextShellFillTarget(level, anchor);
        if (target == null || !MobMindConfig.ENABLE_MOB_BLOCK_PLACING.get()) {
            builder.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, MOVE_SPEED);
            return;
        }

        double targetX = target.getX() + 0.5D;
        double targetY = target.getY();
        double targetZ = target.getZ() + 0.5D;
        if (builder.distanceToSqr(targetX, targetY, targetZ) > WORK_DISTANCE_SQR) {
            builder.getNavigation().moveTo(targetX, targetY, targetZ, MOVE_SPEED);
            return;
        }

        if (!storage.canChangeBlock(level.getGameTime(), MobMindConfig.MAX_BLOCKS_CHANGED_PER_GROUP_PER_MINUTE.getAsInt())) {
            return;
        }

        ItemStack consumed = savedData.consumeBuildingBlock(group.getGroupId());
        if (consumed == null || !(consumed.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        BlockState placeState = blockItem.getBlock().defaultBlockState();
        if (level.setBlock(target, placeState, 3)) {
            builder.swing(InteractionHand.MAIN_HAND, true);
            savedData.setDirty();
        } else {
            savedData.addToInventoryOrDrop(level, group.getGroupId(), target, consumed);
        }
    }
}
