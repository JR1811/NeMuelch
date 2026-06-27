package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;

import java.util.Collection;

public record DummyClearS2CPacket(int dummyId) implements FabricPacket {
    public static final PacketType<DummyClearS2CPacket> TYPE = PacketType.create(NeMuelch.getId("dummy_clear"), DummyClearS2CPacket::read);
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static DummyClearS2CPacket read(PacketByteBuf buf) {
        return new DummyClearS2CPacket(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(dummyId);
    }

    public void send(Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, this);
        }
    }

    public void send(DummyCloseQuarterEntity entity) {
        if (entity.getWorld().isClient()) return;
        this.send(PlayerLookupUtil.trackingAndSelf(entity));
    }
}
