package com.greytaiwolf.mobmind.attachment;

import com.greytaiwolf.mobmind.MobMindMod;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MobMindMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobMindData>> MOB_MIND =
            ATTACHMENT_TYPES.register("mob_mind", () -> AttachmentType.serializable(ModAttachments::createMonsterData).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }

    public static MobMindData get(Monster monster) {
        return monster.getData(MOB_MIND);
    }

    @Nullable
    public static MobMindData getExisting(Monster monster) {
        return monster.getExistingDataOrNull(MOB_MIND);
    }

    private static MobMindData createMonsterData(IAttachmentHolder holder) {
        if (!(holder instanceof Monster)) {
            throw new IllegalStateException("MobMind data can only be attached to Monster entities");
        }

        return new MobMindData();
    }
}
