package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.nemuelch.NeMuelch;

public record DescribeClipboardC2SPacket(String content) implements FabricPacket {
    public static final int MAX_CHARS = 20_000;

    public static final PacketType<DescribeClipboardC2SPacket> TYPE = PacketType.create(
            NeMuelch.getId("describe_from_clipboard"),
            DescribeClipboardC2SPacket::read
    );

    private static DescribeClipboardC2SPacket read(PacketByteBuf buf) {
        String content = buf.readString();
        return new DescribeClipboardC2SPacket(content);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(this.content);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public void sendPacket() {
        ClientPlayNetworking.send(this);
    }
}
