package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.NeMuelch;

import java.util.Collection;

public record WorldRendererReloadS2CPacket() implements FabricPacket {
    public static final PacketType<WorldRendererReloadS2CPacket> TYPE = PacketType.create(NeMuelch.getId("world_rendering_reload"), WorldRendererReloadS2CPacket::read);

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static WorldRendererReloadS2CPacket read(PacketByteBuf buf) {
        return new WorldRendererReloadS2CPacket();
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    public void send(Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, this);
        }
    }
}
