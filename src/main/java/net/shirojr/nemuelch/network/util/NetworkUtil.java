package net.shirojr.nemuelch.network.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class NetworkUtil {
    public static void writeVec3d(PacketByteBuf buf, Vec3d pos) {
        buf.writeDouble(pos.getX());
        buf.writeDouble(pos.getY());
        buf.writeDouble(pos.getZ());
    }

    public static Vec3d readVec3d(PacketByteBuf buf) {
        return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
