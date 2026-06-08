package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;

public record RopeModificationC2SPacket(Vec3d ropePosA, Vec3d ropePosB, int segments, float width, float slack,
                                        boolean stable) implements FabricPacket {
    public static final PacketType<RopeModificationC2SPacket> TYPE = PacketType.create(NeMuelch.getId("rope_modification"), RopeModificationC2SPacket::read);

    public RopeModificationC2SPacket(RopeData ropeData) {
        this(ropeData.pointA(), ropeData.pointB(), ropeData.segments(), ropeData.width(), ropeData.slack(), ropeData.stable());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static RopeModificationC2SPacket read(PacketByteBuf buf) {
        return new RopeModificationC2SPacket(
                Vec3dHelper.fromPacketByteBuf(buf),
                Vec3dHelper.fromPacketByteBuf(buf),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        Vec3dHelper.toPacketByteBuf(buf, ropePosA);
        Vec3dHelper.toPacketByteBuf(buf, ropePosB);
        buf.writeVarInt(this.segments);
        buf.writeFloat(this.width);
        buf.writeFloat(this.slack);
        buf.writeBoolean(this.stable);
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }
}
