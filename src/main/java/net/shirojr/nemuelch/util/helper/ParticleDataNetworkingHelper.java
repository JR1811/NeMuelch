package net.shirojr.nemuelch.util.helper;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.block.entity.ParticleEmitterBlockEntity;

public class ParticleDataNetworkingHelper {
    private ParticleDataNetworkingHelper() {
    }

    public static void addToBuf(PacketByteBuf buf, ParticleEmitterBlockEntity.ParticleData data) {
        buf.writeIdentifier(data.getId());
        writeVec3d(buf, data.getSpawnPos());
        writeVec3d(buf, data.getDelta());
        buf.writeInt(data.getCount());
        buf.writeDouble(data.getSpeed());
    }

    public static ParticleEmitterBlockEntity.ParticleData getFromBuf(PacketByteBuf buf) {
        return new ParticleEmitterBlockEntity.ParticleData(buf.readIdentifier(),
                readVec3d(buf), readVec3d(buf), buf.readInt(), buf.readDouble());
    }

    private static void writeVec3d(PacketByteBuf byteBuf, Vec3d vec3d) {
        byteBuf.writeDouble(vec3d.x);
        byteBuf.writeDouble(vec3d.y);
        byteBuf.writeDouble(vec3d.z);
    }

    private static Vec3d readVec3d(PacketByteBuf byteBuf) {
        double x = byteBuf.readDouble();
        double y = byteBuf.readDouble();
        double z = byteBuf.readDouble();
        return new Vec3d(x, y, z);
    }
}
