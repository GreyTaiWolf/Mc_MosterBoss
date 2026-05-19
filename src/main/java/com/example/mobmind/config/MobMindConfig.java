package com.example.mobmind.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MobMindConfig {
    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_MOB_BLOCK_BREAKING;
    public static final ModConfigSpec.BooleanValue ENABLE_MOB_BLOCK_PLACING;
    public static final ModConfigSpec.IntValue MAX_BLOCKS_CHANGED_PER_GROUP_PER_MINUTE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("worldInteraction");
        ENABLE_MOB_BLOCK_BREAKING = builder
                .comment("Allow MobMind worker zombies to break allowed natural blocks.")
                .define("enableMobBlockBreaking", true);
        ENABLE_MOB_BLOCK_PLACING = builder
                .comment("Allow MobMind builder zombies to place allowed ordinary blocks.")
                .define("enableMobBlockPlacing", true);
        MAX_BLOCKS_CHANGED_PER_GROUP_PER_MINUTE = builder
                .comment("Maximum total break/place operations one group can perform per minute.")
                .defineInRange("maxBlocksChangedPerGroupPerMinute", 24, 1, 240);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    private MobMindConfig() {
    }
}
