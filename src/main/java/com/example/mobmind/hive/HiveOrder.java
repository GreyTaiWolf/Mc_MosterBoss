package com.example.mobmind.hive;

import java.util.Locale;

public enum HiveOrder {
    IDLE,
    GATHER,
    HUNT_PLAYER,
    DEFEND_LEADER,
    SEEK_SHADE,
    BUILD_SHELTER,
    RETREAT,
    DEFEND_NEST,
    BUILD_NEST;

    public static HiveOrder fromName(String name) {
        if (name == null || name.isBlank()) {
            return IDLE;
        }

        try {
            return HiveOrder.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return IDLE;
        }
    }
}
