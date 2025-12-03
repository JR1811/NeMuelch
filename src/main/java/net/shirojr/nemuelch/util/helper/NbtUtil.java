package net.shirojr.nemuelch.util.helper;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NbtUtil {
    private NbtUtil() {

    }

    public static void vec3dToNbt(NbtCompound nbt, String key, Vec3d vec) {
        NbtCompound entryNbt = new NbtCompound();
        entryNbt.putDouble("x", vec.getX());
        entryNbt.putDouble("y", vec.getY());
        entryNbt.putDouble("z", vec.getZ());
        nbt.put(key, entryNbt);
    }

    @Nullable
    public static Vec3d vec3dFromNbt(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return null;
        NbtCompound nbtEntry = nbt.getCompound(key);
        return new Vec3d(
                nbtEntry.getDouble("x"),
                nbtEntry.getDouble("y"),
                nbtEntry.getDouble("z")
        );
    }
}
