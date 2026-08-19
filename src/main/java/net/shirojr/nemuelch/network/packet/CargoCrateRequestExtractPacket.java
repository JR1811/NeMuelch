package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.NeMuelch;

public record CargoCrateRequestExtractPacket(BlockPos pos, int amount) implements FabricPacket {
    public static final PacketType<CargoCrateRequestExtractPacket> TYPE = PacketType.create(
            NeMuelch.getId("cargo_crate_request_extract"), CargoCrateRequestExtractPacket::read
    );

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    private static CargoCrateRequestExtractPacket read(PacketByteBuf buf) {
        return new CargoCrateRequestExtractPacket(buf.readBlockPos(), buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarInt(this.amount);
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }
}
