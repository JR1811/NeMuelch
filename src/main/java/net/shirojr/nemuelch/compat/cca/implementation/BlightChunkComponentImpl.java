package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;

public class BlightChunkComponentImpl implements BlightChunkComponent, AutoSyncedComponent {
    private final Chunk provider;

    private final HashMap<BlockPos, EnumSet<BlightType>> blightedPositions;
    private final EnumMap<BlightType, Integer> blightAmount;
    private final EnumSet<BlightType> completeBlights;
    double completeBlightThreshold;

    public BlightChunkComponentImpl(Chunk chunk) {
        this.provider = chunk;
        this.blightedPositions = new HashMap<>();
        this.blightAmount = new EnumMap<>(BlightType.class);
        for (BlightType cachedValue : BlightType.CACHED_VALUES) {
            this.blightAmount.put(cachedValue, 0);
        }
        this.completeBlights = EnumSet.noneOf(BlightType.class);
        this.completeBlightThreshold = 0.002;   // just a default value (roughly 200 blocks in a chunk)
    }

    @Override
    public Chunk getProvider() {
        return provider;
    }

    @Override
    public double getCompleteBlightThreshold() {
        return completeBlightThreshold;
    }

    @Override
    public void setCompleteBlightThreshold(double normalizedValue) {
        this.completeBlightThreshold = MathHelper.clamp(normalizedValue, 0, 1);
        this.provider.setNeedsSaving(true);
    }

    @Override
    public EnumSet<BlightType> getBlightsOfPos(BlockPos pos) {
        EnumSet<BlightType> blights = EnumSet.noneOf(BlightType.class);
        blights.addAll(this.completeBlights);
        if (blights.size() == BlightType.CACHED_VALUES.length) return blights;
        EnumSet<BlightType> blightTypesOfPos = blightedPositions.get(pos);
        if (blightTypesOfPos == null) return blights;
        blights.addAll(blightTypesOfPos);
        return blights;
    }

    @Override
    public void setBlightsOnPos(BlockPos pos, BlightType... types) {
        EnumSet<BlightType> set = null;
        for (BlightType type : types) {
            if (completeBlights.contains(type)) continue;
            if (set == null) {
                set = blightedPositions.computeIfAbsent(pos, entryPos -> EnumSet.noneOf(BlightType.class));
            }
            if (set.add(type)) {
                Integer currentAmount = this.blightAmount.get(type);
                if (currentAmount != null && currentAmount >= 0) {
                    this.blightAmount.put(type, currentAmount + 1);
                } else {
                    getBlightPosCount(type);
                }
                if (BlightChunkComponent.getNormalizedPortionOfChunk(provider, getBlightPosCount(type)) >= getCompleteBlightThreshold()) {
                    clearAndConvertToCompleteBlight(type);
                }
            }
        }
        if (set != null && set.isEmpty()) {
            blightedPositions.remove(pos);
        }
        this.provider.setNeedsSaving(true);
    }

    @Override
    public void clearAndConvertToCompleteBlight(BlightType type) {
        this.completeBlights.add(type);
        blightedPositions.entrySet().removeIf(entry -> {
            entry.getValue().remove(type);
            return entry.getValue().isEmpty();
        });
        this.provider.setNeedsSaving(true);
    }

    @Override
    public int getBlightPosCount(BlightType type) {
        if (completeBlights.contains(type)) return -1;
        Integer amount = this.blightAmount.get(type);
        if (amount == null) return -1;
        if (amount >= 0) return amount;

        int count = 0;
        for (EnumSet<BlightType> entry : blightedPositions.values()) {
            if (entry.contains(type)) count++;
        }
        this.blightAmount.put(type, count);
        return count;
    }

    @Override
    public boolean isChunkCompletelyBlighted(BlightType... types) {
        if (types.length == 0) {
            for (BlightType cachedValue : BlightType.CACHED_VALUES) {
                if (!completeBlights.contains(cachedValue)) return false;
            }
        } else {
            for (BlightType type : types) {
                if (!completeBlights.contains(type)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public EnumSet<BlightType> getCompleteChunkBlights() {
        return this.completeBlights;
    }

    @Override
    public void clear(boolean blightPositions, boolean completeChunkBlights) {
        if (blightPositions) {
            this.blightedPositions.clear();
            for (BlightType type : BlightType.CACHED_VALUES) {
                if (!completeBlights.contains(type)) {
                    this.blightAmount.put(type, -1);
                }
            }
        }
        if (completeChunkBlights) {
            this.completeBlights.clear();
            for (BlightType type : BlightType.CACHED_VALUES) {
                this.blightAmount.put(type, -1);
            }
        }
        this.provider.setNeedsSaving(true);
    }

    @Override
    public boolean isEmpty() {
        return this.blightedPositions.isEmpty() && this.completeBlights.isEmpty();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        boolean containsCompleteChunkBlight = tag.contains(NbtKeys.COMPLETE_CHUNK_BLIGHTS);
        boolean containsChunkBlight = tag.contains(NbtKeys.CHUNK_BLIGHTS);

        this.clear(containsChunkBlight, containsCompleteChunkBlight);

        if (containsCompleteChunkBlight) {
            for (NbtElement entryNbt : tag.getList(NbtKeys.COMPLETE_CHUNK_BLIGHTS, NbtElement.STRING_TYPE)) {
                this.completeBlights.add(BlightType.fromString(entryNbt.asString()));
            }
        }
        if (containsChunkBlight) {
            NbtList nbtList = tag.getList(NbtKeys.CHUNK_BLIGHTS, NbtElement.COMPOUND_TYPE);
            for (NbtElement listEntry : nbtList) {
                NbtCompound entryNbt = ((NbtCompound) listEntry);

                BlockPos entryPos = BlockPos.fromLong(entryNbt.getLong(NbtKeys.BLOCK_POS));

                EnumSet<BlightType> blightTypes = EnumSet.noneOf(BlightType.class);
                NbtList entryBlightNbtList = entryNbt.getList(NbtKeys.BLIGHT_TYPES, NbtElement.STRING_TYPE);
                for (NbtElement entry : entryBlightNbtList) {
                    blightTypes.add(BlightType.fromString(entry.asString()));
                }

                this.blightedPositions.compute(entryPos, (key, value) -> {
                    if (value == null) {
                        value = EnumSet.noneOf(BlightType.class);
                    }
                    value.addAll(blightTypes);
                    return value;
                });
            }
        }

        if (tag.contains(NbtKeys.THRESHOLD)) {
            this.setCompleteBlightThreshold(tag.getDouble(NbtKeys.THRESHOLD));
        }
        this.provider.setNeedsSaving(true);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList completeBlightsNbtList = new NbtList();
        for (BlightType completeBlight : this.completeBlights) {
            completeBlightsNbtList.add(NbtString.of(completeBlight.asString()));
        }
        tag.put(NbtKeys.COMPLETE_CHUNK_BLIGHTS, completeBlightsNbtList);

        NbtList blightsNbtList = new NbtList();
        for (var entry : this.blightedPositions.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putLong(NbtKeys.BLOCK_POS, entry.getKey().asLong());

            NbtList blightTypesNbtList = new NbtList();
            for (BlightType blightType : entry.getValue()) {
                blightTypesNbtList.add(NbtString.of(blightType.asString()));
            }
            entryNbt.put(NbtKeys.BLIGHT_TYPES, blightTypesNbtList);

            blightsNbtList.add(entryNbt);
        }
        tag.put(NbtKeys.CHUNK_BLIGHTS, blightsNbtList);

        tag.putDouble(NbtKeys.THRESHOLD, this.completeBlightThreshold);
    }
}
