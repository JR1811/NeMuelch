package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

//TODO. add width and slag data for renderer?
public record RopeData(BlockPos pointA, BlockPos pointB) {
    public boolean contains(BlockPos pos) {
        return pointA.equals(pos) || pointB.equals(pos);
    }

    public boolean contains(BlockPos posA, BlockPos posB) {
        return pointA.equals(posA) && pointB.equals(posB) || pointB.equals(posA) && pointA.equals(posB);
    }

    public boolean isLoaded(World world) {
        boolean loadedA = world.getChunkManager().isChunkLoaded(
                ChunkSectionPos.getSectionCoord(pointA.getX()),
                ChunkSectionPos.getSectionCoord(pointA.getZ())
        );
        boolean loadedB = world.getChunkManager().isChunkLoaded(
                ChunkSectionPos.getSectionCoord(pointB.getX()),
                ChunkSectionPos.getSectionCoord(pointB.getZ())
        );
        return loadedA && loadedB;
    }

    public static RopeData fromNbt(NbtCompound nbt) {
        return new RopeData(BlockPos.fromLong(nbt.getLong("a")), BlockPos.fromLong(nbt.getLong("b")));
    }

    public void toNbt(NbtCompound nbt) {
        nbt.putLong("a", this.pointA.asLong());
        nbt.putLong("b", this.pointB.asLong());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RopeData other)) return false;
        return (pointA().equals(other.pointA()) && pointB().equals(other.pointB()))
                || (pointA().equals(other.pointB()) && pointB().equals(other.pointA()));
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pointA().asLong() ^ pointB().asLong());
    }
}
