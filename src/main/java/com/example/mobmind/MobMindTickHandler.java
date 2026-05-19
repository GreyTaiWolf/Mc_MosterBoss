package com.example.mobmind;

import com.example.mobmind.attachment.MobMindData;
import com.example.mobmind.attachment.ModAttachments;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class MobMindTickHandler {
    public static final int PROCESS_INTERVAL_TICKS = 20;
    public static final double NEARBY_SAME_TYPE_RADIUS = 16.0D;
    public static final int LEADER_TICKET_TIMEOUT_TICKS = 100;

    private static final double BASE_AWARENESS_GAIN_PER_SECOND = 0.05D;
    private static final double AGE_BONUS_PER_MINUTE = 0.01D;
    private static final double MAX_AGE_BONUS_PER_SECOND = 0.25D;
    private static final double SAME_TYPE_BONUS_PER_ZOMBIE = 0.03D;
    private static final double MAX_GROUP_BONUS_PER_SECOND = 0.30D;
    private static final double NIGHT_MULTIPLIER = 1.75D;
    private static final int LEADER_TICKET_DISTANCE = 2;
    private static final TicketType<UUID> LEADER_TICKET =
            TicketType.create(MobMindMod.MODID + "_leader", UUID::compareTo, LEADER_TICKET_TIMEOUT_TICKS);

    private MobMindTickHandler() {
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster monster)) {
            return;
        }

        Level level = monster.level();
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!monster.isAlive() || monster.isNoAi() || monster.tickCount % PROCESS_INTERVAL_TICKS != 0) {
            return;
        }

        refreshLeaderLoading(serverLevel, monster);

        if (monster instanceof Zombie zombie) {
            tickZombie(serverLevel, zombie);
        }
    }

    public static AwarenessSnapshot createSnapshot(ServerLevel level, Zombie zombie) {
        MobMindData data = ModAttachments.get(zombie);
        int nearbySameTypeCount = countNearbySameTypeZombies(level, zombie);
        boolean night = level.isNight();
        double gain = computeAwarenessGain(data.getSurvivalTicks(), nearbySameTypeCount, night);
        return new AwarenessSnapshot(data.getSurvivalTicks(), nearbySameTypeCount, night, gain);
    }

    private static void tickZombie(ServerLevel level, Zombie zombie) {
        MobMindData data = ModAttachments.get(zombie);
        data.addSurvivalTicks(PROCESS_INTERVAL_TICKS);

        int nearbySameTypeCount = countNearbySameTypeZombies(level, zombie);
        double gain = computeAwarenessGain(data.getSurvivalTicks(), nearbySameTypeCount, level.isNight());
        data.addAwarenessProgress(gain);
    }

    public static void refreshLeaderLoading(ServerLevel level, Monster monster) {
        MobMindData data = ModAttachments.getExisting(monster);
        if (data == null || !data.isLeader()) {
            return;
        }

        if (!monster.isPersistenceRequired()) {
            monster.setPersistenceRequired();
        }

        ChunkPos chunkPos = monster.chunkPosition();
        level.getChunkSource().addRegionTicket(LEADER_TICKET, chunkPos, LEADER_TICKET_DISTANCE, monster.getUUID(), true);
    }

    private static int countNearbySameTypeZombies(ServerLevel level, Zombie zombie) {
        AABB searchArea = zombie.getBoundingBox().inflate(NEARBY_SAME_TYPE_RADIUS);
        double maxDistanceSqr = NEARBY_SAME_TYPE_RADIUS * NEARBY_SAME_TYPE_RADIUS;

        return level.getEntitiesOfClass(
                Zombie.class,
                searchArea,
                other -> other != zombie
                        && other.isAlive()
                        && !other.isNoAi()
                        && other.getType() == zombie.getType()
                        && other.distanceToSqr(zombie) <= maxDistanceSqr
        ).size();
    }

    private static double computeAwarenessGain(long survivalTicks, int nearbySameTypeCount, boolean night) {
        double survivalMinutes = survivalTicks / 1200.0D;
        double ageBonus = Math.min(MAX_AGE_BONUS_PER_SECOND, survivalMinutes * AGE_BONUS_PER_MINUTE);
        double groupBonus = Math.min(MAX_GROUP_BONUS_PER_SECOND, nearbySameTypeCount * SAME_TYPE_BONUS_PER_ZOMBIE);
        double gain = BASE_AWARENESS_GAIN_PER_SECOND + ageBonus + groupBonus;
        return night ? gain * NIGHT_MULTIPLIER : gain;
    }

    public record AwarenessSnapshot(long survivalTicks, int nearbySameTypeCount, boolean night, double gainPerSecond) {
    }
}
