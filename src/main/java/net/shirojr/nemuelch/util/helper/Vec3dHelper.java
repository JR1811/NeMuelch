package net.shirojr.nemuelch.util.helper;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class Vec3dHelper {
    public static Vec3d fromNbt(NbtCompound nbt) {
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
}
