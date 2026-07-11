package net.shirojr.nemuelch.util.helper;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Vec3dHelper {
    @Nullable
    public static Vec3d fromNbt(NbtCompound nbt) {
        if (!nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z")) return null;
        return new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
    }

    public static void toNbt(NbtCompound nbt, Vec3d vec) {
        nbt.putDouble("x", vec.x);
        nbt.putDouble("y", vec.y);
        nbt.putDouble("z", vec.z);
    }

    public static Vec3d fromPacketByteBuf(PacketByteBuf buf) {
        return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void toPacketByteBuf(PacketByteBuf buf, Vec3d vec) {
        buf.writeDouble(vec.x);
        buf.writeDouble(vec.y);
        buf.writeDouble(vec.z);
    }

    public static Vec3d reflect(Vec3d input, Direction plane) {
        return switch (plane.getAxis()) {
            case X -> input.multiply(-1, 1, 1);
            case Y -> input.multiply(1, -1, 1);
            case Z -> input.multiply(1, 1, -1);
        };
    }
}
