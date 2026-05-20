package com.greytaiwolf.mobmind.item;

import com.greytaiwolf.mobmind.MobMindMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MobMindMod.MODID);

    public static final DeferredHolder<Item, Item> DEBUG_STICK =
            ITEMS.register("debug_stick", () -> new DebugStickItem(new Item.Properties()));

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
