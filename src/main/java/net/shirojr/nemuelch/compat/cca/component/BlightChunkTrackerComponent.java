package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BlightChunkTrackerComponent implements Component {
    public static final Identifier KEY = NeMuelch.getId("blight_chunk_tracker");

    private final Scoreboard scoreboard;
    @SuppressWarnings("FieldCanBeLocal")
    private final @Nullable MinecraftServer server;

    private final HashSet<ChunkPos> blightedChunks;

    public BlightChunkTrackerComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.scoreboard = scoreboard;
        this.server = server;
        this.blightedChunks = new HashSet<>();
    }

    public Scoreboard getProvider() {
        return scoreboard;
    }

    public static BlightChunkTrackerComponent get(ServerWorld world) {
        return NeMuelchComponents.BLIGHT_CHUNK_TRACKER.get(world.getScoreboard());
    }

    public Set<ChunkPos> getAllBlightedChunks() {
        return Collections.unmodifiableSet(blightedChunks);
    }

    public void addBlightedChunk(ChunkPos chunk) {
        this.blightedChunks.add(chunk);
    }

    public void removeBlightedChunk(ChunkPos chunk) {
        this.blightedChunks.remove(chunk);
    }

    public void clearBlightedChunks(ServerWorld world) {
        HashSet<ChunkPos> entries = new HashSet<>(this.getAllBlightedChunks());
        for (ChunkPos entry : entries) {
            WorldChunk chunk = world.getChunk(entry.x, entry.z);
            Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(chunk);
            if (blightChunkComponent.isEmpty()) continue;
            BlightChunkComponent component = blightChunkComponent.get();
            component.clear(true, true, true);
        }
        this.blightedChunks.clear();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.blightedChunks.clear();
        if (tag.contains(NbtKeys.TRACKED_BLIGHTED_CHUNKS)) {
            for (NbtElement nbtElement : tag.getList(NbtKeys.TRACKED_BLIGHTED_CHUNKS, NbtElement.LONG_TYPE)) {
                this.blightedChunks.add(new ChunkPos(((NbtLong) nbtElement).longValue()));
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        NbtList nbtList = new NbtList();
        for (ChunkPos blightedChunk : this.blightedChunks) {
            nbtList.add(NbtLong.of(blightedChunk.toLong()));
        }
        tag.put(NbtKeys.TRACKED_BLIGHTED_CHUNKS, nbtList);
    }
}
