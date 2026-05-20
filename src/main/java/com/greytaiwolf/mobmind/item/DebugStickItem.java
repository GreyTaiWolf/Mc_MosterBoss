package com.greytaiwolf.mobmind.item;

import com.greytaiwolf.mobmind.attachment.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DebugStickItem extends Item {
    public DebugStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.PASS;
        }

        if (!(target instanceof Monster monster)) {
            player.sendSystemMessage(Component.literal("目标不是怪物"));
            return InteractionResult.PASS;
        }

        if (!player.isCreative() && !player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("调试棒只能在创造模式或管理员权限下使用。"));
            return InteractionResult.FAIL;
        }

        ModAttachments.get(monster).addAwareness(100);
        int awareness = ModAttachments.get(monster).getAwareness();
        player.sendSystemMessage(Component.literal("Awareness: " + awareness));
        return InteractionResult.SUCCESS;
    }
}
