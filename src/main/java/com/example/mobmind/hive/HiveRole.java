package com.example.mobmind.hive;

import java.util.Locale;

public enum HiveRole {
    NONE,
    LEADER,
    MINER,
    GUARD,
    PATROL,
    BUILDER;

    public static HiveRole fromName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }

        try {
            return HiveRole.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
