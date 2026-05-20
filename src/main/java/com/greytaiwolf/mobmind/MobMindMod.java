package com.greytaiwolf.mobmind;

import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.config.MobMindConfig;
import com.greytaiwolf.mobmind.event.CombatEventHandler;
import com.greytaiwolf.mobmind.event.LevelTickHandler;
import com.greytaiwolf.mobmind.item.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MobMindMod.MODID)
public class MobMindMod {
    public static final String MODID = "mobmind";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobMindMod(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModItems.register(modEventBus);
        modEventBus.addListener(this::onBuildCreativeModeTabContents);
        modContainer.registerConfig(ModConfig.Type.SERVER, MobMindConfig.SERVER_SPEC);
        NeoForge.EVENT_BUS.addListener(MobMindTickHandler::onEntityTick);
        NeoForge.EVENT_BUS.addListener(LevelTickHandler::onLevelTick);
        NeoForge.EVENT_BUS.addListener(CombatEventHandler::onLivingIncomingDamage);
        NeoForge.EVENT_BUS.addListener(CombatEventHandler::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MobMindDebugCommands::register);
        LOGGER.info("MobMind initialized");
    }

    private void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.DEBUG_STICK);
        }
    }
}

