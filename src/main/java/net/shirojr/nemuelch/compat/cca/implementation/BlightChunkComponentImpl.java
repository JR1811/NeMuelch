package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;

import java.util.BitSet;

public class BlightChunkComponentImpl implements BlightChunkComponent, AutoSyncedComponent {
    private final Chunk provider;

    private BitSet witheredAreas = new BitSet(256);
    private BitSet poisonousAreas = new BitSet(256);
    private BitSet corruptedAreas = new BitSet(256);
    private BitSet spreadingAreas = new BitSet(256);

    private BitSet allBlightedAreas = null;
    private boolean cacheValid = false;

    public BlightChunkComponentImpl(Chunk chunk) {
        this.provider = chunk;
    }

    @Override
    public Chunk getProvider() {
        return provider;
    }

    public boolean hasBlightType(int localX, int localZ, BlightType type) {
        return getBitSetForType(type).get(getIndex(localX, localZ));
    }

    public void setBlightType(int localX, int localZ, BlightType type, boolean blighted) {
        getBitSetForType(type).set(getIndex(localX, localZ), blighted);
        invalidateCache();
    }

    public boolean isAnyBlighted(int localX, int localZ) {
        if (!cacheValid) {
            rebuildCache();
        }
        return allBlightedAreas.get(getIndex(localX, localZ));
    }

    private void rebuildCache() {
        allBlightedAreas = (BitSet) witheredAreas.clone();
        allBlightedAreas.or(poisonousAreas);
        allBlightedAreas.or(corruptedAreas);
        allBlightedAreas.or(spreadingAreas);
        cacheValid = true;
    }

    private void invalidateCache() {
        cacheValid = false;
    }

    public void spreadBlightType(BlightType type, BitSet newAreas) {
        getBitSetForType(type).or(newAreas);
        invalidateCache();
    }

    public boolean hasAnyBlightTypes(int localX, int localZ, BlightType... types) {
        int index = getIndex(localX, localZ);
        if (types.length == 0) {
            return witheredAreas.get(index) ||
                    poisonousAreas.get(index) ||
                    corruptedAreas.get(index) ||
                    spreadingAreas.get(index);
        }
        for (BlightType type : types) {
            if (!getBitSetForType(type).get(index)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        witheredAreas = loadBitSetFromNbt(tag, "Withered");
        poisonousAreas = loadBitSetFromNbt(tag, "Poisonous");
        corruptedAreas = loadBitSetFromNbt(tag, "Corrupted");
        spreadingAreas = loadBitSetFromNbt(tag, "Spreading");
        invalidateCache();
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLongArray("Withered", witheredAreas.toLongArray());
        tag.putLongArray("Poisonous", poisonousAreas.toLongArray());
        tag.putLongArray("Corrupted", corruptedAreas.toLongArray());
        tag.putLongArray("Spreading", spreadingAreas.toLongArray());
    }

    // --------------------------- Utility ---------------------------

    public static int getIndex(int localX, int localZ) {
        return localZ * 16 + localX;
    }

    private BitSet getBitSetForType(BlightType type) {
        return switch (type) {
            case WITHERING -> this.witheredAreas;
            case POISONOUS -> this.poisonousAreas;
            case CORRUPTED -> this.corruptedAreas;
            case SPREADING -> this.spreadingAreas;
        };
    }

    private BitSet loadBitSetFromNbt(NbtCompound tag, String key) {
        if (tag.contains(key, NbtElement.LONG_ARRAY_TYPE)) {
            return BitSet.valueOf(tag.getLongArray(key));
        }
        return new BitSet(256);
    }
}
