package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.NeMuelch;

import java.util.HashSet;
import java.util.Set;

public record BlockFinderResultS2CPacket(Set<BlockPos> result) implements FabricPacket {
    public static final PacketType<BlockFinderResultS2CPacket> TYPE = PacketType.create(NeMuelch.getId("block_finder_result"), BlockFinderResultS2CPacket::read);

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static BlockFinderResultS2CPacket read(PacketByteBuf buf) {
        return new BlockFinderResultS2CPacket(new HashSet<>(buf.readList(PacketByteBuf::readBlockPos)));
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.result, PacketByteBuf::writeBlockPos);
    }

    public void send(ServerPlayerEntity target) {
        ServerPlayNetworking.send(target, this);
    }
}
