package com.example.mobmind.hive;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HiveSavedData extends net.minecraft.world.level.saveddata.SavedData {
    public static final int SHARED_INVENTORY_SIZE = 27 * 4;
    private static final String FILE_ID = "mobmind_hives";
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_INVENTORY = "inventory";
    private static final String KEY_SHELTER = "shelterAnchor";
    private static final String KEY_MINING_TARGET = "miningTarget";
    private static final String KEY_MINING_PROGRESS = "miningProgress";
    private static final String KEY_BLOCK_CHANGE_WINDOW_START = "blockChangeWindowStart";
    private static final String KEY_BLOCK_CHANGES_IN_WINDOW = "blockChangesInWindow";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";

    private final Map<UUID, GroupStorage> groups = new HashMap<>();

    public static HiveSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(HiveSavedData::new, HiveSavedData::load), FILE_ID);
    }

    public static HiveSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        HiveSavedData data = new HiveSavedData();
        ListTag groupsTag = tag.getList(KEY_GROUPS, Tag.TAG_COMPOUND);
        for (int i = 0; i < groupsTag.size(); i++) {
            CompoundTag groupTag = groupsTag.getCompound(i);
            UUID groupId;
            try {
                groupId = UUID.fromString(groupTag.getString(KEY_GROUP_ID));
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            GroupStorage storage = new GroupStorage();
            if (groupTag.contains(KEY_INVENTORY, Tag.TAG_LIST)) {
                storage.inventory.fromTag(groupTag.getList(KEY_INVENTORY, Tag.TAG_COMPOUND), provider);
            }
            storage.shelterAnchor = readBlockPos(groupTag, KEY_SHELTER);
            storage.miningTarget = readBlockPos(groupTag, KEY_MINING_TARGET);
            storage.miningProgress = Math.max(0, groupTag.getInt(KEY_MINING_PROGRESS));
            storage.blockChangeWindowStart = Math.max(0L, groupTag.getLong(KEY_BLOCK_CHANGE_WINDOW_START));
            storage.blockChangesInWindow = Math.max(0, groupTag.getInt(KEY_BLOCK_CHANGES_IN_WINDOW));
            data.groups.put(groupId, storage);
        }
        return data;
    }

    public GroupStorage getOrCreate(UUID groupId) {
        GroupStorage storage = groups.get(groupId);
        if (storage == null) {
            storage = new GroupStorage();
            groups.put(groupId, storage);
            setDirty();
        }
        return storage;
    }

    public void removeGroup(UUID groupId) {
        if (groups.remove(groupId) != null) {
            setDirty();
        }
    }

    @Nullable
    public GroupStorage getGroup(UUID groupId) {
        return groups.get(groupId);
    }

    public void dropAndRemoveGroup(ServerLevel level, UUID groupId, BlockPos dropPos) {
        GroupStorage storage = groups.remove(groupId);
        if (storage == null) {
            return;
        }

        for (int i = 0; i < storage.inventory.getContainerSize(); i++) {
            ItemStack stack = storage.inventory.getItem(i);
            if (!stack.isEmpty()) {
                Block.popResource(level, dropPos, stack.copy());
            }
        }
        setDirty();
    }

    public void addToInventoryOrDrop(ServerLevel level, UUID groupId, BlockPos dropPos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        GroupStorage storage = getOrCreate(groupId);
        ItemStack remainder = storage.inventory.addItem(stack.copy());
        if (!remainder.isEmpty()) {
            Block.popResource(level, dropPos, remainder);
        }
        setDirty();
    }

    @Nullable
    public ItemStack consumeBuildingBlock(UUID groupId) {
        GroupStorage storage = groups.get(groupId);
        if (storage == null) {
            return null;
        }

        for (int i = 0; i < storage.inventory.getContainerSize(); i++) {
            ItemStack stack = storage.inventory.getItem(i);
            if (BlockChangeRules.isOrdinaryBuildingBlock(stack) && stack.getItem() instanceof BlockItem blockItem) {
                ItemStack consumed = storage.inventory.removeItem(i, 1);
                setDirty();
                return consumed;
            }
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag groupsTag = new ListTag();
        for (Map.Entry<UUID, GroupStorage> entry : groups.entrySet()) {
            CompoundTag groupTag = new CompoundTag();
            groupTag.putString(KEY_GROUP_ID, entry.getKey().toString());
            GroupStorage storage = entry.getValue();
            groupTag.put(KEY_INVENTORY, storage.inventory.createTag(provider));
            putBlockPos(groupTag, KEY_SHELTER, storage.shelterAnchor);
            putBlockPos(groupTag, KEY_MINING_TARGET, storage.miningTarget);
            groupTag.putInt(KEY_MINING_PROGRESS, storage.miningProgress);
            groupTag.putLong(KEY_BLOCK_CHANGE_WINDOW_START, storage.blockChangeWindowStart);
            groupTag.putInt(KEY_BLOCK_CHANGES_IN_WINDOW, storage.blockChangesInWindow);
            groupsTag.add(groupTag);
        }
        tag.put(KEY_GROUPS, groupsTag);
        return tag;
    }

    @Nullable
    private static BlockPos readBlockPos(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt(KEY_X), posTag.getInt(KEY_Y), posTag.getInt(KEY_Z));
    }

    private static void putBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }

        CompoundTag posTag = new CompoundTag();
        posTag.putInt(KEY_X, pos.getX());
        posTag.putInt(KEY_Y, pos.getY());
        posTag.putInt(KEY_Z, pos.getZ());
        tag.put(key, posTag);
    }

    public static class GroupStorage {
        private final SimpleContainer inventory = new SimpleContainer(SHARED_INVENTORY_SIZE);
        @Nullable
        private BlockPos shelterAnchor;
        @Nullable
        private BlockPos miningTarget;
        private int miningProgress;
        private long blockChangeWindowStart;
        private int blockChangesInWindow;

        public SimpleContainer getInventory() {
            return inventory;
        }

        @Nullable
        public BlockPos getShelterAnchor() {
            return shelterAnchor;
        }

        public void setShelterAnchor(@Nullable BlockPos shelterAnchor) {
            this.shelterAnchor = shelterAnchor;
        }

        @Nullable
        public BlockPos getMiningTarget() {
            return miningTarget;
        }

        public void setMiningTarget(@Nullable BlockPos miningTarget) {
            this.miningTarget = miningTarget;
        }

        public int getMiningProgress() {
            return miningProgress;
        }

        public void setMiningProgress(int miningProgress) {
            this.miningProgress = Math.max(0, miningProgress);
        }

        public boolean canChangeBlock(long gameTime, int limitPerMinute) {
            if (gameTime - blockChangeWindowStart >= 1200L) {
                blockChangeWindowStart = gameTime;
                blockChangesInWindow = 0;
            }
            if (blockChangesInWindow >= limitPerMinute) {
                return false;
            }
            blockChangesInWindow++;
            return true;
        }
    }
}
