package com.example.mobmind.attachment;

import com.example.mobmind.hive.HiveRole;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class MobMindData implements INBTSerializable<CompoundTag> {
    public static final String ORDER_IDLE = "IDLE";

    private static final String KEY_AWARENESS = "awareness";
    private static final String KEY_AWARENESS_PROGRESS = "awarenessProgress";
    private static final String KEY_SURVIVAL_TICKS = "survivalTicks";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_LEADER_UUID = "leaderUuid";
    private static final String KEY_LEADER = "leader";
    private static final String KEY_LEADER_LEVEL = "leaderLevel";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ROLE_ASSIGNED_AT = "roleAssignedAt";
    private static final String KEY_CURRENT_ORDER = "currentOrder";
    private static final String KEY_NEST = "nest";
    private static final String KEY_NEST_X = "x";
    private static final String KEY_NEST_Y = "y";
    private static final String KEY_NEST_Z = "z";
    private static final String KEY_ORIGINAL_MAIN_HAND = "originalMainHand";
    private static final String KEY_ORIGINAL_OFF_HAND = "originalOffHand";
    private static final String KEY_ORIGINAL_HEAD = "originalHead";
    private static final String KEY_ORIGINAL_MAIN_HAND_DROP_CHANCE = "originalMainHandDropChance";
    private static final String KEY_ORIGINAL_OFF_HAND_DROP_CHANCE = "originalOffHandDropChance";
    private static final String KEY_ORIGINAL_HEAD_DROP_CHANCE = "originalHeadDropChance";
    private static final String KEY_EQUIPMENT_SNAPSHOT_CAPTURED = "equipmentSnapshotCaptured";

    private int awareness;
    private double awarenessProgress;
    private long survivalTicks;
    @Nullable
    private UUID groupId;
    @Nullable
    private UUID leaderUuid;
    private boolean leader;
    private int leaderLevel;
    private HiveRole role = HiveRole.NONE;
    private long roleAssignedAt;
    private String currentOrder = ORDER_IDLE;
    @Nullable
    private BlockPos nestPos;
    private ItemStack originalMainHand = ItemStack.EMPTY;
    private ItemStack originalOffHand = ItemStack.EMPTY;
    private ItemStack originalHead = ItemStack.EMPTY;
    private float originalMainHandDropChance;
    private float originalOffHandDropChance;
    private float originalHeadDropChance;
    private boolean equipmentSnapshotCaptured;

    public int getAwareness() {
        return awareness;
    }

    public void setAwareness(int awareness) {
        this.awareness = Math.max(0, awareness);
    }

    public void addAwareness(int amount) {
        setAwareness(this.awareness + Math.max(0, amount));
    }

    public double getAwarenessProgress() {
        return awarenessProgress;
    }

    public void setAwarenessProgress(double awarenessProgress) {
        this.awarenessProgress = Double.isFinite(awarenessProgress) ? Math.max(0.0D, awarenessProgress) : 0.0D;
    }

    public int addAwarenessProgress(double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0D) {
            return 0;
        }

        double nextProgress = awarenessProgress + amount;
        int wholeAwareness = (int)Math.floor(nextProgress);
        awarenessProgress = nextProgress - wholeAwareness;
        addAwareness(wholeAwareness);
        return wholeAwareness;
    }

    public void resetAwareness() {
        awareness = 0;
        awarenessProgress = 0.0D;
    }

    public long getSurvivalTicks() {
        return survivalTicks;
    }

    public void setSurvivalTicks(long survivalTicks) {
        this.survivalTicks = Math.max(0L, survivalTicks);
    }

    public void addSurvivalTicks(long ticks) {
        if (ticks <= 0L) {
            return;
        }

        survivalTicks = Math.min(Long.MAX_VALUE - ticks, survivalTicks) + ticks;
    }

    @Nullable
    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable UUID groupId) {
        this.groupId = groupId;
    }

    @Nullable
    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(@Nullable UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public int getLeaderLevel() {
        return leaderLevel;
    }

    public void setLeaderLevel(int leaderLevel) {
        this.leaderLevel = Math.max(0, leaderLevel);
    }

    public HiveRole getRole() {
        return role;
    }

    public void setRole(HiveRole role) {
        this.role = role == null ? HiveRole.NONE : role;
    }

    public long getRoleAssignedAt() {
        return roleAssignedAt;
    }

    public void setRoleAssignedAt(long roleAssignedAt) {
        this.roleAssignedAt = Math.max(0L, roleAssignedAt);
    }

    public String getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(String currentOrder) {
        this.currentOrder = currentOrder == null || currentOrder.isBlank() ? ORDER_IDLE : currentOrder;
    }

    @Nullable
    public BlockPos getNestPos() {
        return nestPos;
    }

    public void setNestPos(@Nullable BlockPos nestPos) {
        this.nestPos = nestPos;
    }

    public boolean isDefault() {
        return awareness == 0
                && awarenessProgress == 0.0D
                && survivalTicks == 0L
                && groupId == null
                && leaderUuid == null
                && !leader
                && leaderLevel == 0
                && role == HiveRole.NONE
                && roleAssignedAt == 0L
                && ORDER_IDLE.equals(currentOrder)
                && nestPos == null
                && !equipmentSnapshotCaptured;
    }

    public ItemStack getOriginalMainHand() {
        return originalMainHand.copy();
    }

    public void setOriginalMainHand(ItemStack originalMainHand) {
        this.originalMainHand = originalMainHand == null ? ItemStack.EMPTY : originalMainHand.copy();
    }

    public ItemStack getOriginalOffHand() {
        return originalOffHand.copy();
    }

    public void setOriginalOffHand(ItemStack originalOffHand) {
        this.originalOffHand = originalOffHand == null ? ItemStack.EMPTY : originalOffHand.copy();
    }

    public ItemStack getOriginalHead() {
        return originalHead.copy();
    }

    public void setOriginalHead(ItemStack originalHead) {
        this.originalHead = originalHead == null ? ItemStack.EMPTY : originalHead.copy();
    }

    public float getOriginalMainHandDropChance() {
        return originalMainHandDropChance;
    }

    public void setOriginalMainHandDropChance(float originalMainHandDropChance) {
        this.originalMainHandDropChance = originalMainHandDropChance;
    }

    public float getOriginalOffHandDropChance() {
        return originalOffHandDropChance;
    }

    public void setOriginalOffHandDropChance(float originalOffHandDropChance) {
        this.originalOffHandDropChance = originalOffHandDropChance;
    }

    public float getOriginalHeadDropChance() {
        return originalHeadDropChance;
    }

    public void setOriginalHeadDropChance(float originalHeadDropChance) {
        this.originalHeadDropChance = originalHeadDropChance;
    }

    public boolean isEquipmentSnapshotCaptured() {
        return equipmentSnapshotCaptured;
    }

    public void setEquipmentSnapshotCaptured(boolean equipmentSnapshotCaptured) {
        this.equipmentSnapshotCaptured = equipmentSnapshotCaptured;
    }

    public void clearGroup() {
        this.groupId = null;
        this.leaderUuid = null;
        this.leader = false;
        this.leaderLevel = 0;
        this.role = HiveRole.NONE;
        this.roleAssignedAt = 0L;
        this.currentOrder = ORDER_IDLE;
    }

    public void clearEquipmentSnapshot() {
        this.originalMainHand = ItemStack.EMPTY;
        this.originalOffHand = ItemStack.EMPTY;
        this.originalHead = ItemStack.EMPTY;
        this.originalMainHandDropChance = 0.0F;
        this.originalOffHandDropChance = 0.0F;
        this.originalHeadDropChance = 0.0F;
        this.equipmentSnapshotCaptured = false;
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        if (isDefault()) {
            return null;
        }

        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_AWARENESS, awareness);
        tag.putDouble(KEY_AWARENESS_PROGRESS, awarenessProgress);
        tag.putLong(KEY_SURVIVAL_TICKS, survivalTicks);
        tag.putBoolean(KEY_LEADER, leader);
        tag.putInt(KEY_LEADER_LEVEL, leaderLevel);
        tag.putString(KEY_ROLE, role.name());
        tag.putLong(KEY_ROLE_ASSIGNED_AT, roleAssignedAt);
        tag.putString(KEY_CURRENT_ORDER, currentOrder);

        if (groupId != null) {
            tag.putUUID(KEY_GROUP_ID, groupId);
        }
        if (leaderUuid != null) {
            tag.putUUID(KEY_LEADER_UUID, leaderUuid);
        }
        if (nestPos != null) {
            CompoundTag nestTag = new CompoundTag();
            nestTag.putInt(KEY_NEST_X, nestPos.getX());
            nestTag.putInt(KEY_NEST_Y, nestPos.getY());
            nestTag.putInt(KEY_NEST_Z, nestPos.getZ());
            tag.put(KEY_NEST, nestTag);
        }
        if (!originalMainHand.isEmpty()) {
            tag.put(KEY_ORIGINAL_MAIN_HAND, originalMainHand.save(provider));
        }
        if (!originalOffHand.isEmpty()) {
            tag.put(KEY_ORIGINAL_OFF_HAND, originalOffHand.save(provider));
        }
        if (!originalHead.isEmpty()) {
            tag.put(KEY_ORIGINAL_HEAD, originalHead.save(provider));
        }
        tag.putFloat(KEY_ORIGINAL_MAIN_HAND_DROP_CHANCE, originalMainHandDropChance);
        tag.putFloat(KEY_ORIGINAL_OFF_HAND_DROP_CHANCE, originalOffHandDropChance);
        tag.putFloat(KEY_ORIGINAL_HEAD_DROP_CHANCE, originalHeadDropChance);
        tag.putBoolean(KEY_EQUIPMENT_SNAPSHOT_CAPTURED, equipmentSnapshotCaptured);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        awareness = Math.max(0, tag.getInt(KEY_AWARENESS));
        setAwarenessProgress(tag.contains(KEY_AWARENESS_PROGRESS, Tag.TAG_DOUBLE) ? tag.getDouble(KEY_AWARENESS_PROGRESS) : 0.0D);
        survivalTicks = Math.max(0L, tag.getLong(KEY_SURVIVAL_TICKS));
        leader = tag.getBoolean(KEY_LEADER);
        leaderLevel = Math.max(0, tag.getInt(KEY_LEADER_LEVEL));
        role = tag.contains(KEY_ROLE, Tag.TAG_STRING) ? HiveRole.fromName(tag.getString(KEY_ROLE)) : HiveRole.NONE;
        roleAssignedAt = Math.max(0L, tag.getLong(KEY_ROLE_ASSIGNED_AT));
        currentOrder = tag.contains(KEY_CURRENT_ORDER, Tag.TAG_STRING) ? tag.getString(KEY_CURRENT_ORDER) : ORDER_IDLE;
        groupId = tag.hasUUID(KEY_GROUP_ID) ? tag.getUUID(KEY_GROUP_ID) : null;
        leaderUuid = tag.hasUUID(KEY_LEADER_UUID) ? tag.getUUID(KEY_LEADER_UUID) : null;

        if (tag.contains(KEY_NEST, Tag.TAG_COMPOUND)) {
            CompoundTag nestTag = tag.getCompound(KEY_NEST);
            nestPos = new BlockPos(nestTag.getInt(KEY_NEST_X), nestTag.getInt(KEY_NEST_Y), nestTag.getInt(KEY_NEST_Z));
        } else {
            nestPos = null;
        }

        if (currentOrder == null || currentOrder.isBlank()) {
            currentOrder = ORDER_IDLE;
        }

        originalMainHand = tag.contains(KEY_ORIGINAL_MAIN_HAND, Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(provider, tag.getCompound(KEY_ORIGINAL_MAIN_HAND))
                : ItemStack.EMPTY;
        originalOffHand = tag.contains(KEY_ORIGINAL_OFF_HAND, Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(provider, tag.getCompound(KEY_ORIGINAL_OFF_HAND))
                : ItemStack.EMPTY;
        originalHead = tag.contains(KEY_ORIGINAL_HEAD, Tag.TAG_COMPOUND)
                ? ItemStack.parseOptional(provider, tag.getCompound(KEY_ORIGINAL_HEAD))
                : ItemStack.EMPTY;
        originalMainHandDropChance = tag.contains(KEY_ORIGINAL_MAIN_HAND_DROP_CHANCE, Tag.TAG_FLOAT)
                ? tag.getFloat(KEY_ORIGINAL_MAIN_HAND_DROP_CHANCE)
                : 0.0F;
        originalOffHandDropChance = tag.contains(KEY_ORIGINAL_OFF_HAND_DROP_CHANCE, Tag.TAG_FLOAT)
                ? tag.getFloat(KEY_ORIGINAL_OFF_HAND_DROP_CHANCE)
                : 0.0F;
        originalHeadDropChance = tag.contains(KEY_ORIGINAL_HEAD_DROP_CHANCE, Tag.TAG_FLOAT)
                ? tag.getFloat(KEY_ORIGINAL_HEAD_DROP_CHANCE)
                : 0.0F;
        equipmentSnapshotCaptured = tag.getBoolean(KEY_EQUIPMENT_SNAPSHOT_CAPTURED);
    }
}
