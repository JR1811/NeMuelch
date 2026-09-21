package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.NeMuelch;

import java.util.Collection;

public record RequestDescribeClipboardS2CPacket(int maxContentLength)
        implements FabricPacket {

    public static final PacketType<RequestDescribeClipboardS2CPacket> TYPE = PacketType.create(
            NeMuelch.getId("request_describe_from_clipboard"),
            RequestDescribeClipboardS2CPacket::read
    );

    private static RequestDescribeClipboardS2CPacket read(PacketByteBuf buf) {
        int maxContentLength = buf.readVarInt();
        return new RequestDescribeClipboardS2CPacket(maxContentLength);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.maxContentLength);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public void sendPacket(Collection<ServerPlayerEntity> targets) {
        targets.forEach(target -> ServerPlayNetworking.send(target, this));
    }
}
