package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.helper.NbtUtil;

public record RopeData(Vec3d pointA, Vec3d pointB) {
    public RopeData(BlockPos posA, BlockPos posB) {
        this(posA.toCenterPos(), posB.toCenterPos());
    }

    public boolean contains(Vec3d pos) {
        return pointA.equals(pos) || pointB.equals(pos);
    }

    public boolean contains(Vec3d posA, Vec3d posB) {
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
        return loadedA || loadedB;
    }

    public static RopeData fromNbt(NbtCompound nbt) {
        return new RopeData(NbtUtil.vec3dFromNbt(nbt, "a"), NbtUtil.vec3dFromNbt(nbt, "b"));
    }

    public void toNbt(NbtCompound nbt) {
        NbtUtil.vec3dToNbt(nbt, "a", this.pointA);
        NbtUtil.vec3dToNbt(nbt, "b", this.pointB);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RopeData other)) return false;
        return (pointA().equals(other.pointA()) && pointB().equals(other.pointB()))
                || (pointA().equals(other.pointB()) && pointB().equals(other.pointA()));
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pointA().hashCode() ^ pointB().hashCode());
    }
}
