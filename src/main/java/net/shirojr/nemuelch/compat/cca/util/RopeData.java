package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.helper.NbtUtil;

public record RopeData(Vec3d pointA, Vec3d pointB, int segments, float width, float slack, boolean stable) {
    public RopeData(Vec3d posA, Vec3d posB, float slack) {
        this(posA, posB, getApproximatedSegmentCount(posA, posB, slack, 4), 0.025f, slack, true);
    }

    public RopeData(Vec3d posA, Vec3d posB) {
        this(posA, posB, 3.5f);
    }

    public RopeData(Vec3d posA, Vec3d posB, boolean stable) {
        this(posA, posB, getApproximatedSegmentCount(posA, posB, 3.5f, 4), 0.025f, 3.5f, stable);
    }


    public static int getApproximatedSegmentCount(Vec3d posA, Vec3d posB, float slack, double segmentsPerBlock) {
        int curveSamples = 16;
        double arcLength = 0;
        Vec3d delta = posB.subtract(posA);
        Vec3d previous = posA;
        for (int sampleIndex = 0; sampleIndex < curveSamples; sampleIndex++) {
            float normalizedIndex = (float) sampleIndex / curveSamples;
            double indexSag = slack * normalizedIndex * (normalizedIndex - 1.0F);
            double x = posA.x + delta.x * normalizedIndex;
            double y = posA.y + (delta.y > 0 ?
                    delta.y * normalizedIndex * normalizedIndex :
                    delta.y - delta.y * (1.0F - normalizedIndex) * (1.0F - normalizedIndex));
            y += indexSag;
            double z = posA.z + delta.z * normalizedIndex;
            Vec3d current = new Vec3d(x, y, z);
            arcLength += current.distanceTo(previous);
            previous = current;
        }
        return (int) MathHelper.clamp(Math.round(arcLength * segmentsPerBlock), 4, 128);
    }

    public boolean contains(Vec3d pos) {
        return pointA.equals(pos) || pointB.equals(pos);
    }

    public boolean contains(BlockPos pos) {
        return BlockPos.ofFloored(pointA).equals(pos) || BlockPos.ofFloored(pointB).equals(pos);
    }

    public boolean contains(Vec3d posA, Vec3d posB) {
        return pointA.equals(posA) && pointB.equals(posB) || pointB.equals(posA) && pointA.equals(posB);
    }

    public boolean isUnloaded(World world) {
        boolean loadedA = world.getChunkManager().isChunkLoaded(
                ChunkSectionPos.getSectionCoord(pointA.getX()),
                ChunkSectionPos.getSectionCoord(pointA.getZ())
        );
        boolean loadedB = world.getChunkManager().isChunkLoaded(
                ChunkSectionPos.getSectionCoord(pointB.getX()),
                ChunkSectionPos.getSectionCoord(pointB.getZ())
        );
        return !loadedA && !loadedB;
    }

    public static RopeData fromNbt(NbtCompound nbt) {
        return new RopeData(
                NbtUtil.vec3dFromNbt(nbt, "a"),
                NbtUtil.vec3dFromNbt(nbt, "b"),
                nbt.getInt("segments"),
                nbt.getFloat("width"),
                nbt.getFloat("slack"),
                nbt.getBoolean("stable")
        );
    }

    public void toNbt(NbtCompound nbt) {
        NbtUtil.vec3dToNbt(nbt, "a", this.pointA);
        NbtUtil.vec3dToNbt(nbt, "b", this.pointB);
        nbt.putInt("segments", this.segments);
        nbt.putFloat("width", this.width);
        nbt.putFloat("slack", this.slack);
        nbt.putBoolean("stable", this.stable);
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
