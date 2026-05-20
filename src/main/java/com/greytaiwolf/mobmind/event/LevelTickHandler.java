package com.greytaiwolf.mobmind.event;

import com.greytaiwolf.mobmind.ai.OrderExecutor;
import com.greytaiwolf.mobmind.hive.HiveSelector;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class LevelTickHandler {
    private LevelTickHandler() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.getGameTime() % HiveSelector.SCAN_INTERVAL_TICKS == 0L) {
            HiveSelector.scan(level);
        }
        if (level.getGameTime() % OrderExecutor.EXECUTE_INTERVAL_TICKS == 0L) {
            OrderExecutor.tick(level);
        }
    }
}
