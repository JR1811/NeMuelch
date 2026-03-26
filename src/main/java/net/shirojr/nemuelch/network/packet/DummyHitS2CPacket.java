package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;

import java.util.Collection;

public record DummyHitS2CPacket(int entityId, float damage, float angleInRad) implements FabricPacket {
    public static final PacketType<DummyHitS2CPacket> TYPE = PacketType.create(NeMuelch.getId("dummy_hit"), DummyHitS2CPacket::read);
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static DummyHitS2CPacket read(PacketByteBuf buf) {
        return new DummyHitS2CPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(damage);
        buf.writeFloat(angleInRad);
    }

    public void send(Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, this);
        }
    }

    public void send(DummyCloseQuarterEntity entity) {
        if (entity.getWorld().isClient()) return;
        this.send(PlayerLookup.tracking(entity));
    }
}
