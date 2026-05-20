package com.greytaiwolf.mobmind;

import com.greytaiwolf.mobmind.attachment.MobMindData;
import com.greytaiwolf.mobmind.attachment.ModAttachments;
import com.greytaiwolf.mobmind.hive.HiveSavedData;
import com.greytaiwolf.mobmind.hive.HiveSelector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MobMindDebugCommands {
    private MobMindDebugCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(MobMindMod.MODID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")
                        .then(Commands.literal("awareness")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("zombie", EntityArgument.entity())
                                                .executes(MobMindDebugCommands::getAwareness)))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("zombie", EntityArgument.entity())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(MobMindDebugCommands::setAwareness))))
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("zombie", EntityArgument.entity())
                                                .executes(MobMindDebugCommands::resetAwareness))))
                        .then(Commands.literal("group")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("monster", EntityArgument.entity())
                                                .executes(MobMindDebugCommands::getGroup))))
                        .then(Commands.literal("leader")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("monster", EntityArgument.entity())
                                                .executes(MobMindDebugCommands::getLeader)))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("monster", EntityArgument.entity())
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(MobMindDebugCommands::setLeader)))))));
    }

    private static int getAwareness(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Zombie zombie = getZombie(context);
        if (zombie == null) {
            return 0;
        }

        if (!(zombie.level() instanceof ServerLevel level)) {
            context.getSource().sendFailure(Component.literal("MobMind debug commands must run on the logical server."));
            return 0;
        }

        MobMindData data = ModAttachments.get(zombie);
        MobMindTickHandler.AwarenessSnapshot snapshot = MobMindTickHandler.createSnapshot(level, zombie);
        context.getSource().sendSuccess(
                () -> Component.literal(String.format(
                        Locale.ROOT,
                        "%s awareness=%d progress=%.3f survival=%.1fs nearbySameType=%d night=%s nextGain=%.3f/s",
                        describe(zombie),
                        data.getAwareness(),
                        data.getAwarenessProgress(),
                        snapshot.survivalTicks() / 20.0D,
                        snapshot.nearbySameTypeCount(),
                        snapshot.night(),
                        snapshot.gainPerSecond()
                )),
                false
        );
        return data.getAwareness();
    }

    private static int setAwareness(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Zombie zombie = getZombie(context);
        if (zombie == null) {
            return 0;
        }

        int value = IntegerArgumentType.getInteger(context, "value");
        MobMindData data = ModAttachments.get(zombie);
        data.setAwareness(value);
        data.setAwarenessProgress(0.0D);

        context.getSource().sendSuccess(
                () -> Component.literal(String.format(Locale.ROOT, "%s awareness set to %d", describe(zombie), value)),
                true
        );
        return value;
    }

    private static int resetAwareness(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Zombie zombie = getZombie(context);
        if (zombie == null) {
            return 0;
        }

        MobMindData data = ModAttachments.get(zombie);
        data.resetAwareness();

        context.getSource().sendSuccess(
                () -> Component.literal(String.format(Locale.ROOT, "%s awareness reset", describe(zombie))),
                true
        );
        return 1;
    }

    private static int getGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Monster monster = getMonster(context);
        if (monster == null) {
            return 0;
        }

        MobMindData data = ModAttachments.get(monster);
        int usedSlots = 0;
        int totalItems = 0;
        if (monster.level() instanceof ServerLevel level && data.getGroupId() != null) {
            HiveSavedData.GroupStorage storage = HiveSavedData.get(level).getGroup(data.getGroupId());
            if (storage != null) {
                for (int i = 0; i < storage.getInventory().getContainerSize(); i++) {
                    ItemStack stack = storage.getInventory().getItem(i);
                    if (!stack.isEmpty()) {
                        usedSlots++;
                        totalItems += stack.getCount();
                    }
                }
            }
        }
        int finalUsedSlots = usedSlots;
        int finalTotalItems = totalItems;
        context.getSource().sendSuccess(
                () -> Component.literal(String.format(
                        Locale.ROOT,
                        "%s group=%s leaderUuid=%s leader=%s leaderLevel=%d role=%s order=%s roleAssignedAt=%d sharedInventory=%d/%d slots items=%d",
                        describe(monster),
                        data.getGroupId(),
                        data.getLeaderUuid(),
                        data.isLeader(),
                        data.getLeaderLevel(),
                        data.getRole(),
                        data.getCurrentOrder(),
                        data.getRoleAssignedAt(),
                        finalUsedSlots,
                        HiveSavedData.SHARED_INVENTORY_SIZE,
                        finalTotalItems
                )),
                false
        );
        return data.getGroupId() == null ? 0 : 1;
    }

    private static int getLeader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Monster monster = getMonster(context);
        if (monster == null) {
            return 0;
        }

        MobMindData data = ModAttachments.get(monster);
        context.getSource().sendSuccess(
                () -> Component.literal(String.format(
                        Locale.ROOT,
                        "%s leader=%s persistent=%s chunk=%s",
                        describe(monster),
                        data.isLeader(),
                        monster.isPersistenceRequired(),
                        monster.chunkPosition()
                )),
                false
        );
        return data.isLeader() ? 1 : 0;
    }

    private static int setLeader(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Monster monster = getMonster(context);
        if (monster == null) {
            return 0;
        }

        boolean value = BoolArgumentType.getBool(context, "value");
        MobMindData data = ModAttachments.get(monster);
        data.setLeader(value);
        if (value && monster.level() instanceof ServerLevel level) {
            MobMindTickHandler.refreshLeaderLoading(level, monster);
            if (monster instanceof Zombie zombie) {
                HiveSelector.applyLeaderPresentation(zombie);
            }
        } else if (!value) {
            monster.setGlowingTag(false);
            monster.removeEffect(net.minecraft.world.effect.MobEffects.GLOWING);
        }

        context.getSource().sendSuccess(
                () -> Component.literal(String.format(Locale.ROOT, "%s leader set to %s", describe(monster), value)),
                true
        );
        return value ? 1 : 0;
    }

    private static Zombie getZombie(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "zombie");
        if (entity instanceof Zombie zombie) {
            return zombie;
        }

        context.getSource().sendFailure(Component.literal("Selected entity is not a zombie."));
        return null;
    }

    private static Monster getMonster(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "monster");
        if (entity instanceof Monster monster) {
            return monster;
        }

        context.getSource().sendFailure(Component.literal("Selected entity is not a monster."));
        return null;
    }

    private static String describe(Zombie zombie) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(zombie.getType()) + " " + zombie.getUUID();
    }

    private static String describe(Monster monster) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(monster.getType()) + " " + monster.getUUID();
    }
}
