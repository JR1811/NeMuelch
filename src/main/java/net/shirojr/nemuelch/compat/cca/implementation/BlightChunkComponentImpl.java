package net.shirojr.nemuelch.compat.cca.implementation;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightSpreader;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.*;

public class BlightChunkComponentImpl implements BlightChunkComponent {

    private final Chunk provider;

    private final HashMap<BlockPos, EnumSet<BlightType>> blightedPositions;
    private final EnumMap<BlightType, Integer> blightAmount;
    private final EnumSet<BlightType> completeBlights;

    private double completeBlightThreshold;
    private long tick;
    private long timeOfFirstBlight;

    private final BlightSpreader spreader;


    public BlightChunkComponentImpl(Chunk chunk) {
        this.provider = chunk;
        this.blightedPositions = new HashMap<>();
        this.blightAmount = new EnumMap<>(BlightType.class);
        for (BlightType cachedValue : BlightType.CACHED_VALUES) {
            this.blightAmount.put(cachedValue, 0);
        }
        this.completeBlights = EnumSet.noneOf(BlightType.class);
        this.completeBlightThreshold = BlightChunkComponent.getNormalizedPortionOfChunk(provider, 16 * 16 * 3);
        this.tick = 0;

        // this.tickOffset = provider.getPos().toLong() % TICK_SPEED;
        this.timeOfFirstBlight = -1;

        this.spreader = new BlightSpreader(this);
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
        this.markDirty();
    }

    @Override
    public long getTimeOfFirstInitializedBlight() {
        return timeOfFirstBlight;
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
    public HashSet<BlockPos> getPosWithBlights(BlightType... types) {
        EnumSet<BlightType> targetTypes = BlightType.typesToEnumSet(types);
        HashSet<BlockPos> result = new HashSet<>();
        if (types.length == 0) {
            result.addAll(blightedPositions.keySet());
            return result;
        }
        for (var entry : this.blightedPositions.entrySet()) {
            if (!Collections.disjoint(entry.getValue(), targetTypes)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public void addBlightsToPos(BlockPos pos, Set<BlightType> types) {
        if (types.isEmpty()) return;
        BlockState state = provider.getBlockState(pos);
        ServerWorld serverWorld = getServerWorld();
        boolean initiallyBlight = isBlighted(pos);

        EnumSet<BlightType> set = null;
        for (BlightType type : types) {
            if (completeBlights.contains(type)) continue;
            if (!type.getActions().get().canBlight(serverWorld, pos, state, types)) continue;
            if (set == null) {
                set = blightedPositions.computeIfAbsent(pos, entryPos -> EnumSet.noneOf(BlightType.class));
            }
            if (set.add(type)) {
                Integer currentAmount = this.blightAmount.get(type);
                boolean resultsInFullChunk = false;
                if (currentAmount != null && currentAmount >= 0) {
                    this.blightAmount.put(type, currentAmount + 1);
                } else {
                    getBlightPosCount(type);
                }
                if (BlightChunkComponent.getNormalizedPortionOfChunk(provider, getBlightPosCount(type)) >= getCompleteBlightThreshold()) {
                    clearAndConvertToCompleteBlight(type);
                    resultsInFullChunk = true;
                }
                if (serverWorld != null) {
                    type.getActions().get().onApplied(serverWorld, resultsInFullChunk ? null : pos, null);
                }
            }
        }
        if (set != null && set.isEmpty()) {
            blightedPositions.remove(pos);
        }
        if (!initiallyBlight && isBlighted(pos) && provider instanceof WorldChunk worldChunk) {
            this.timeOfFirstBlight = worldChunk.getWorld().getTime();
        }
        this.markDirty();
    }

    @Override
    public void clearAndConvertToCompleteBlight(BlightType type) {
        this.completeBlights.add(type);
        blightedPositions.entrySet().removeIf(entry -> {
            entry.getValue().remove(type);
            return entry.getValue().isEmpty();
        });
        this.markDirty();
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

    public void decrementBlightPosCount(BlightType type) {
        Integer amount = this.blightAmount.get(type);
        if (amount == null || amount <= 0) {
            this.blightAmount.put(type, -1);
        } else {
            this.blightAmount.put(type, amount - 1);
        }

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
    public boolean contains(BlightType... types) {
        for (BlightType type : types) {
            if (completeBlights.contains(type)) continue;
            if (getBlightPosCount(type) == 0) return false;
        }
        return true;
    }

    @Override
    public EnumSet<BlightType> getCompleteChunkBlights() {
        return this.completeBlights;
    }

    @Override
    public void clear(boolean blightPositions, boolean completeChunkBlights, boolean markDirty) {
        ServerWorld serverWorld = getServerWorld();
        if (blightPositions) {
            if (serverWorld != null) {
                for (var entry : this.blightedPositions.entrySet()) {
                    entry.getValue().forEach(type -> type.getActions().get().onRemoved(serverWorld, entry.getKey(), getTimeOfFirstInitializedBlight(), null));
                }
            }
            this.blightedPositions.clear();
            for (BlightType type : BlightType.CACHED_VALUES) {
                if (!completeBlights.contains(type)) {
                    this.blightAmount.put(type, -1);
                }
            }
        }
        if (completeChunkBlights) {
            if (serverWorld != null) {
                for (BlightType completeBlight : this.completeBlights) {
                    completeBlight.getActions().get().onRemoved(serverWorld, null, getTimeOfFirstInitializedBlight(), null);
                }
            }
            this.completeBlights.clear();
            for (BlightType type : BlightType.CACHED_VALUES) {
                this.blightAmount.put(type, -1);
            }
        }
        if (this.blightedPositions.isEmpty() && this.completeBlights.isEmpty()) {
            this.timeOfFirstBlight = -1;
        }
        if (markDirty) {
            this.markDirty();
        }
    }

    @Override
    public void clear(BlightType... types) {
        ServerWorld serverWorld = getServerWorld();
        for (BlightType type : types) {
            this.blightedPositions.entrySet().removeIf(entry -> {
                boolean shouldBeRemoved = entry.getValue().contains(type);
                if (!shouldBeRemoved) return false;
                if (serverWorld != null) {
                    entry.getValue().forEach(entryType -> entryType.getActions().get().onRemoved(
                            serverWorld, entry.getKey(), getTimeOfFirstInitializedBlight(), null
                    ));
                }
                return true;
            });
            boolean removeCompleteBlight = this.completeBlights.remove(type);
            if (removeCompleteBlight && serverWorld != null) {
                type.getActions().get().onRemoved(serverWorld, null, getTimeOfFirstInitializedBlight(), null);
            }
            this.blightAmount.put(type, -1);
        }
        if (this.blightedPositions.isEmpty() && this.completeBlights.isEmpty()) {
            this.timeOfFirstBlight = -1;
        }
        this.markDirty();
    }

    @Override
    public void clearPos(BlockPos pos, Set<BlightType> types) {
        ServerWorld serverWorld = getServerWorld();
        EnumSet<BlightType> posBlights = this.blightedPositions.get(pos);
        if (posBlights == null) return;
        if (types.isEmpty()) {
            this.blightedPositions.remove(pos);
            for (BlightType removedBlight : posBlights) {
                if (!completeBlights.contains(removedBlight)) {
                    if (serverWorld != null) {
                        removedBlight.getActions().get().onRemoved(serverWorld, pos, getTimeOfFirstInitializedBlight(), null);
                    }
                    decrementBlightPosCount(removedBlight);
                }
            }
        } else {
            boolean anyRemoved = false;
            for (BlightType type : types) {
                if (!completeBlights.contains(type) && posBlights.remove(type)) {
                    anyRemoved = true;
                    if (serverWorld != null) {
                        type.getActions().get().onRemoved(serverWorld, pos, getTimeOfFirstInitializedBlight(), null);
                    }
                    decrementBlightPosCount(type);
                }
            }
            if (anyRemoved && posBlights.isEmpty()) {
                this.blightedPositions.remove(pos);
            }
        }
        if (this.blightedPositions.isEmpty() && this.completeBlights.isEmpty()) {
            this.timeOfFirstBlight = -1;
        }

        this.markDirty();
    }

    @Override
    public boolean isEmpty() {
        return this.blightedPositions.isEmpty() && this.completeBlights.isEmpty();
    }

    @Override
    public void serverTick() {
        ServerWorld world = getServerWorld();
        if (world == null) return;
        if (!world.getGameRules().getBoolean(NemuelchGameRules.BLIGHT_SPREADING)) return;
        if (isEmpty()) return;
        if (!contains(BlightType.SPREADING)) return;
        world.getProfiler().push("nemuelch_blight_server_tick");

        this.tick++;
        if ((this.tick) % world.getGameRules().getInt(NemuelchGameRules.BLIGHT_TICK_SPEED) != 0) return;
        if (getCompleteChunkBlights().contains(BlightType.SPREADING) && world.getGameRules().getBoolean(NemuelchGameRules.BLIGHT_SPREADING_CHUNKS)) {
            world.getProfiler().push("nemuelch_blight_server_tick_spread_full_chunk");
            this.spreader.spreadFromCompleteChunk(world);
            world.getProfiler().pop();
        } else {
            world.getProfiler().push("nemuelch_blight_server_tick_spread_partial_chunk");
            this.spreader.spreadFromPartialChunk(world);
            world.getProfiler().pop();
        }
        world.getProfiler().pop();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        boolean containsCompleteChunkBlight = tag.contains(NbtKeys.COMPLETE_CHUNK_BLIGHTS);
        boolean containsChunkBlight = tag.contains(NbtKeys.CHUNK_BLIGHTS);

        this.clear(containsChunkBlight, containsCompleteChunkBlight, false);

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
            this.completeBlightThreshold = tag.getDouble(NbtKeys.THRESHOLD);
        }
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

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return PlayerLookup.tracking(player.getServerWorld(), provider.getPos()).contains(player);
    }

    @Override
    public void markDirty() {
        if (getServerWorld() == null) return;
        this.sync();
        this.provider.setNeedsSaving(true);
    }
}
