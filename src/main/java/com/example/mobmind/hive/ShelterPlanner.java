package com.example.mobmind.hive;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ShelterPlanner {
    public static final int HALF_SIZE = 7;
    public static final int HEIGHT = 3;
    public static final int SEARCH_RADIUS = 48;

    private ShelterPlanner() {
    }

    public static boolean isSunExposed(ServerLevel level, Zombie zombie) {
        return level.isDay() && level.canSeeSky(BlockPos.containing(zombie.getX(), zombie.getEyeY(), zombie.getZ()));
    }

    @Nullable
    public static BlockPos findShadeAnchor(ServerLevel level, Zombie leader) {
        BlockPos origin = leader.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int radius = 2; radius <= SEARCH_RADIUS; radius += 2) {
            for (int dx = -radius; dx <= radius; dx += 2) {
                for (int dz = -radius; dz <= radius; dz += 2) {
                    for (int dy = -8; dy <= 6; dy++) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        if (!isSafeStand(level, pos)) {
                            continue;
                        }

                        double distance = pos.distSqr(origin);
                        if (distance < bestDistance) {
                            best = pos;
                            bestDistance = distance;
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }

        return origin.below(3);
    }

    public static boolean isSafeStand(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())) {
            return false;
        }

        return !level.canSeeSky(pos.above())
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).blocksMotion();
    }

    @Nullable
    public static BlockPos findNextInteriorBreakTarget(ServerLevel level, BlockPos anchor) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
                for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                    BlockPos pos = anchor.offset(x, y, z);
                    if (BlockChangeRules.canBreak(level, pos)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static BlockPos findNextShellFillTarget(ServerLevel level, BlockPos anchor) {
        for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
            for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
                BlockPos floor = anchor.offset(x, -1, z);
                if (BlockChangeRules.canPlace(level, floor)) {
                    return floor;
                }
                BlockPos roof = anchor.offset(x, HEIGHT, z);
                if (BlockChangeRules.canPlace(level, roof)) {
                    return roof;
                }
            }
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int i = -HALF_SIZE; i <= HALF_SIZE; i++) {
                BlockPos north = anchor.offset(i, y, -HALF_SIZE - 1);
                if (BlockChangeRules.canPlace(level, north)) {
                    return north;
                }
                BlockPos south = anchor.offset(i, y, HALF_SIZE + 1);
                if (BlockChangeRules.canPlace(level, south)) {
                    return south;
                }
                BlockPos west = anchor.offset(-HALF_SIZE - 1, y, i);
                if (BlockChangeRules.canPlace(level, west)) {
                    return west;
                }
                BlockPos east = anchor.offset(HALF_SIZE + 1, y, i);
                if (BlockChangeRules.canPlace(level, east)) {
                    return east;
                }
            }
        }
        return null;
    }

    public static boolean isWorthDigging(BlockState state) {
        return !state.isAir();
    }
}
