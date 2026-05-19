package com.example.mobmind.ai;

import com.example.mobmind.config.MobMindConfig;
import com.example.mobmind.hive.BlockChangeRules;
import com.example.mobmind.hive.HiveData;
import com.example.mobmind.hive.HiveSavedData;
import com.example.mobmind.hive.ShelterPlanner;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MinerRoleExecutor {
    private static final int BREAK_PROGRESS_COMPLETE = 6;
    private static final double WORK_DISTANCE_SQR = 20.0D;
    private static final double MOVE_SPEED = 1.0D;

    private MinerRoleExecutor() {
    }

    public static void execute(ServerLevel level, HiveData group, Zombie leader, Zombie miner) {
        HiveSavedData savedData = HiveSavedData.get(level);
        HiveSavedData.GroupStorage storage = savedData.getOrCreate(group.getGroupId());
        BlockPos anchor = storage.getShelterAnchor();
        if (anchor == null) {
            anchor = ShelterPlanner.findShadeAnchor(level, leader);
            storage.setShelterAnchor(anchor);
            savedData.setDirty();
        }

        if (!MobMindConfig.ENABLE_MOB_BLOCK_BREAKING.get()) {
            miner.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, MOVE_SPEED);
            return;
        }

        BlockPos target = getOrChooseTarget(level, storage, anchor);
        if (target == null) {
            miner.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, MOVE_SPEED);
            return;
        }

        double targetX = target.getX() + 0.5D;
        double targetY = target.getY() + 0.5D;
        double targetZ = target.getZ() + 0.5D;
        if (miner.distanceToSqr(targetX, targetY, targetZ) > WORK_DISTANCE_SQR) {
            miner.getNavigation().moveTo(targetX, targetY, targetZ, MOVE_SPEED);
            return;
        }

        mineTarget(level, savedData, storage, group, miner, target);
    }

    @Nullable
    private static BlockPos getOrChooseTarget(ServerLevel level, HiveSavedData.GroupStorage storage, BlockPos anchor) {
        BlockPos target = storage.getMiningTarget();
        if (target != null && BlockChangeRules.canBreak(level, target)) {
            return target;
        }

        target = ShelterPlanner.findNextInteriorBreakTarget(level, anchor);
        storage.setMiningTarget(target);
        storage.setMiningProgress(0);
        return target;
    }

    private static void mineTarget(
            ServerLevel level,
            HiveSavedData savedData,
            HiveSavedData.GroupStorage storage,
            HiveData group,
            Zombie miner,
            BlockPos target
    ) {
        BlockState state = level.getBlockState(target);
        if (!ShelterPlanner.isWorthDigging(state) || !BlockChangeRules.canBreak(level, target)) {
            level.destroyBlockProgress(miner.getId(), target, -1);
            storage.setMiningTarget(null);
            storage.setMiningProgress(0);
            savedData.setDirty();
            return;
        }

        ItemStack tool = BlockChangeRules.shouldUseShovel(state) ? new ItemStack(Items.IRON_SHOVEL) : new ItemStack(Items.IRON_PICKAXE);
        miner.setItemSlot(EquipmentSlot.MAINHAND, tool);
        miner.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        miner.swing(InteractionHand.MAIN_HAND, true);

        int progress = storage.getMiningProgress() + 1;
        storage.setMiningProgress(progress);
        level.destroyBlockProgress(miner.getId(), target, Math.min(9, progress));

        if (progress < BREAK_PROGRESS_COMPLETE) {
            savedData.setDirty();
            return;
        }

        if (!storage.canChangeBlock(level.getGameTime(), MobMindConfig.MAX_BLOCKS_CHANGED_PER_GROUP_PER_MINUTE.getAsInt())) {
            storage.setMiningProgress(BREAK_PROGRESS_COMPLETE - 1);
            savedData.setDirty();
            return;
        }

        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(target) : null;
        List<ItemStack> drops = Block.getDrops(state, level, target, blockEntity, miner, tool);
        boolean destroyed = level.destroyBlock(target, false, miner, 512);
        level.destroyBlockProgress(miner.getId(), target, -1);
        storage.setMiningTarget(null);
        storage.setMiningProgress(0);

        if (destroyed) {
            for (ItemStack drop : drops) {
                savedData.addToInventoryOrDrop(level, group.getGroupId(), target, drop);
            }
        }
        savedData.setDirty();
    }
}
