package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.util.helper.Vec3dHelper;

public record RopeDeletionC2SPacket(Vec3d ropePosA, Vec3d ropePosB) implements FabricPacket {
    public static final PacketType<RopeDeletionC2SPacket> TYPE = PacketType.create(NeMuelch.getId("rope_deletion"), RopeDeletionC2SPacket::read);

    public RopeDeletionC2SPacket(RopeData ropeData) {
        this(ropeData.pointA(), ropeData.pointB());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static RopeDeletionC2SPacket read(PacketByteBuf buf) {
        return new RopeDeletionC2SPacket(
                Vec3dHelper.fromPacketByteBuf(buf),
                Vec3dHelper.fromPacketByteBuf(buf)
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        Vec3dHelper.toPacketByteBuf(buf, ropePosA);
        Vec3dHelper.toPacketByteBuf(buf, ropePosB);
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }
}
