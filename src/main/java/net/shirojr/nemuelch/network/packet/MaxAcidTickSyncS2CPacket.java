package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NemuelchGameRules;

import java.util.Collection;

public record MaxAcidTickSyncS2CPacket(int maxTick) implements FabricPacket {
    public static final PacketType<MaxAcidTickSyncS2CPacket> TYPE = PacketType.create(NeMuelch.getId("max_acid_tick_sync"), MaxAcidTickSyncS2CPacket::read);

    public MaxAcidTickSyncS2CPacket(ServerWorld world) {
        this(world.getGameRules().getInt(NemuelchGameRules.ACIDIC_ATMOSPHERE_MAX_TICKS));
    }

    private static MaxAcidTickSyncS2CPacket read(PacketByteBuf buf) {
        return new MaxAcidTickSyncS2CPacket(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.maxTick);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public void send(Collection<ServerPlayerEntity> targets) {
        targets.forEach(player -> ServerPlayNetworking.send(player, this));
    }
}
