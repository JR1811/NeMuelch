package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.NeMuelch;

public record BlockFinderActiveS2CPacket(boolean active) implements FabricPacket {
    public static final PacketType<BlockFinderActiveS2CPacket> TYPE = PacketType.create(NeMuelch.getId("block_finder_active"), BlockFinderActiveS2CPacket::read);

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static BlockFinderActiveS2CPacket read(PacketByteBuf buf) {
        return new BlockFinderActiveS2CPacket(buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(active);
    }

    public void send(ServerPlayerEntity target) {
        ServerPlayNetworking.send(target, this);
    }
}
