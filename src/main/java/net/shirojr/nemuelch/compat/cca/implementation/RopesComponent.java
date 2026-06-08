package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RopesComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("ropes");

    private final List<RopeData> ropes;
    private final Long2ObjectOpenHashMap<List<RopeData>> indexedUnstableRopes;
    private final World world;

    public RopesComponent(World world) {
        this.world = world;
        this.ropes = new ArrayList<>();
        this.indexedUnstableRopes = new Long2ObjectOpenHashMap<>();
    }

    public static RopesComponent get(World world) {
        return NeMuelchComponents.ROPES.get(world);
    }

    public World getProvider() {
        return this.world;
    }

    public List<RopeData> getRopes() {
        return Collections.unmodifiableList(this.ropes);
    }

    public List<RopeData> getUnstableRopesInChunk(long chunkKey) {
        List<RopeData> chunkRopes = indexedUnstableRopes.get(chunkKey);
        if (chunkRopes == null) return Collections.emptyList();
        return Collections.unmodifiableList(chunkRopes);
    }

    public void modifyRopes(boolean sync, Consumer<List<RopeData>> entries) {
        entries.accept(this.ropes);
        this.rebuildIndex();
        if (sync) this.sync();
    }

    private void indexUnstableRopeData(RopeData ropeData) {
        if (ropeData.stable()) return;
        long keyA = getChunkKey(ropeData.pointA().getX(), ropeData.pointA().getZ());
        long keyB = getChunkKey(ropeData.pointB().getX(), ropeData.pointB().getZ());
        this.indexedUnstableRopes.computeIfAbsent(keyA, chunkKey -> new ArrayList<>()).add(ropeData);
        if (keyA != keyB) {
            this.indexedUnstableRopes.computeIfAbsent(keyB, chunkKey -> new ArrayList<>()).add(ropeData);
        }
    }

    private void rebuildIndex() {
        this.indexedUnstableRopes.clear();
        for (RopeData rope : ropes) {
            indexUnstableRopeData(rope);
        }
    }

    public boolean isEmpty() {
        return this.ropes.isEmpty();
    }

    public static long getChunkKey(double x, double z) {
        return ChunkPos.toLong(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {
        this.ropes.clear();
        NbtList ropesNbt = nbt.getList("ropes", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < ropesNbt.size(); i++) {
            this.ropes.add(RopeData.fromNbt(ropesNbt.getCompound(i)));
        }
        this.rebuildIndex();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {
        NbtList ropesNbt = new NbtList();
        for (RopeData rope : this.ropes) {
            NbtCompound ropeNbt = new NbtCompound();
            rope.toNbt(ropeNbt);
            ropesNbt.add(ropeNbt);
        }
        nbt.put("ropes", ropesNbt);
    }

    public void sync() {
        NeMuelchComponents.ROPES.sync(this.world);
    }
}
