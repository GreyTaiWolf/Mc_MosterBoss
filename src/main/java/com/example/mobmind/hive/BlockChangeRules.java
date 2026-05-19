package com.example.mobmind.hive;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockChangeRules {
    private static final Set<Block> SENSITIVE_BLOCKS = Set.of(
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.BARREL,
            Blocks.ENDER_CHEST,
            Blocks.SPAWNER,
            Blocks.COMMAND_BLOCK,
            Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.REDSTONE_WIRE,
            Blocks.REDSTONE_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.LEVER,
            Blocks.OBSERVER,
            Blocks.PISTON,
            Blocks.STICKY_PISTON,
            Blocks.HOPPER,
            Blocks.DISPENSER,
            Blocks.DROPPER,
            Blocks.NOTE_BLOCK,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.TNT,
            Blocks.WATER,
            Blocks.LAVA
    );

    private static final Set<Block> ORDINARY_BUILDING_BLOCKS = Set.of(
            Blocks.COBBLESTONE,
            Blocks.COBBLED_DEEPSLATE,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.STONE,
            Blocks.DEEPSLATE,
            Blocks.ANDESITE,
            Blocks.DIORITE,
            Blocks.GRANITE
    );

    private BlockChangeRules() {
    }

    public static boolean canBreak(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || isSensitive(state)) {
            return false;
        }

        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES)
                || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    public static boolean canPlace(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (!(state.isAir() || state.is(BlockTags.REPLACEABLE)) || isSensitive(state)) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            if (isSensitive(level.getBlockState(pos.relative(direction)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOrdinaryBuildingBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && ORDINARY_BUILDING_BLOCKS.contains(blockItem.getBlock())
                && !blockItem.getBlock().defaultBlockState().hasBlockEntity();
    }

    public static boolean shouldUseShovel(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                && !state.is(BlockTags.BASE_STONE_OVERWORLD)
                && !state.is(BlockTags.STONE_ORE_REPLACEABLES)
                && !state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    private static boolean isSensitive(BlockState state) {
        return state.hasBlockEntity()
                || SENSITIVE_BLOCKS.contains(state.getBlock())
                || state.is(BlockTags.BEDS)
                || state.is(BlockTags.DOORS)
                || state.is(BlockTags.TRAPDOORS)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.BUTTONS)
                || state.is(BlockTags.PRESSURE_PLATES)
                || state.is(BlockTags.RAILS);
    }
}
