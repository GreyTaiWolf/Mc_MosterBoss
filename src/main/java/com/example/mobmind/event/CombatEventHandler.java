package com.example.mobmind.event;

import com.example.mobmind.attachment.MobMindData;
import com.example.mobmind.attachment.ModAttachments;
import com.example.mobmind.ai.DefendLeaderOrder;
import com.example.mobmind.hive.HiveManager;
import com.example.mobmind.hive.HiveOrder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import com.example.mobmind.species.SpeciesProfiles;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class CombatEventHandler {
    private CombatEventHandler() {
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || SpeciesProfiles.resolve(zombie) == null) {
            return;
        }
        if (!(zombie.level() instanceof ServerLevel level)) {
            return;
        }

        MobMindData data = ModAttachments.getExisting(zombie);
        if (data == null || !data.isLeader() || data.getGroupId() == null) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof Player player && player.isAlive() && !player.isSpectator() && !player.isCreative()) {
            HiveManager.setGroupTarget(level, data.getGroupId(), player.getUUID(), HiveOrder.DEFEND_LEADER);
            HiveManager.getGroup(level, data.getGroupId()).ifPresent(group -> DefendLeaderOrder.execute(level, group, zombie, player));
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || SpeciesProfiles.resolve(zombie) == null) {
            return;
        }
        if (!(zombie.level() instanceof ServerLevel level)) {
            return;
        }

        MobMindData data = ModAttachments.getExisting(zombie);
        if (data != null && data.isLeader() && data.getGroupId() != null) {
            HiveManager.disbandGroup(level, data.getGroupId(), zombie.blockPosition());
        }
    }
}
