package net.shirojr.nemuelch.util.data;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public record WorldChunkPos(ServerWorld world, ChunkPos pos) {
}
