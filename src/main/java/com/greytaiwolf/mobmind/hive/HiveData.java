package com.greytaiwolf.mobmind.hive;

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class HiveData {
    private static final ResourceLocation DEFAULT_SPECIES_ID = ResourceLocation.fromNamespaceAndPath("mobmind", "zombie");
    private final UUID groupId;
    private UUID leaderUuid;
    private ResourceLocation speciesId;
    private int leaderLevel;
    private int capacity;
    private int simulatedMinerals;
    @Nullable
    private UUID targetUuid;
    private HiveOrder currentOrder;
    @Nullable
    private UUID pendingThreatUuid;
    private long lastThreatReportTick;
    private int threatLevel;

    public HiveData(UUID groupId, UUID leaderUuid, HiveOrder currentOrder) {
        this(groupId, leaderUuid, 1, 6, currentOrder);
    }

    public HiveData(UUID groupId, UUID leaderUuid, int leaderLevel, int capacity, HiveOrder currentOrder) {
        this.groupId = groupId;
        this.leaderUuid = leaderUuid;
        this.speciesId = DEFAULT_SPECIES_ID;
        this.leaderLevel = Math.max(1, leaderLevel);
        this.capacity = Math.max(1, capacity);
        this.currentOrder = currentOrder;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public ResourceLocation getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(ResourceLocation speciesId) {
        this.speciesId = speciesId == null ? DEFAULT_SPECIES_ID : speciesId;
    }

    public int getLeaderLevel() {
        return leaderLevel;
    }

    public void setLeaderLevel(int leaderLevel) {
        this.leaderLevel = Math.max(1, leaderLevel);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public int getSimulatedMinerals() {
        return simulatedMinerals;
    }

    public void addSimulatedMinerals(int amount) {
        if (amount > 0) {
            simulatedMinerals = Math.min(Integer.MAX_VALUE - amount, simulatedMinerals) + amount;
        }
    }

    @Nullable
    public UUID getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(@Nullable UUID targetUuid) {
        this.targetUuid = targetUuid;
    }

    public HiveOrder getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(HiveOrder currentOrder) {
        this.currentOrder = currentOrder;
    }

    @Nullable
    public UUID getPendingThreatUuid() {
        return pendingThreatUuid;
    }

    public void setPendingThreatUuid(@Nullable UUID pendingThreatUuid) {
        this.pendingThreatUuid = pendingThreatUuid;
    }

    public long getLastThreatReportTick() {
        return lastThreatReportTick;
    }

    public void setLastThreatReportTick(long lastThreatReportTick) {
        this.lastThreatReportTick = Math.max(0L, lastThreatReportTick);
    }

    public int getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(int threatLevel) {
        this.threatLevel = Math.max(0, threatLevel);
    }

    public void clearThreatReport() {
        pendingThreatUuid = null;
        lastThreatReportTick = 0L;
        threatLevel = 0;
    }
}

