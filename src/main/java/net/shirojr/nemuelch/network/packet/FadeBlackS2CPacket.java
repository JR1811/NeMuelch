package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;

import java.util.Collection;

public record FadeBlackS2CPacket(int duration, float targetValue) implements FabricPacket {
    public static final PacketType<FadeBlackS2CPacket> TYPE = PacketType.create(NeMuelch.getId("fading_shader"), FadeBlackS2CPacket::read);

    public FadeBlackS2CPacket(int duration, float targetValue) {
        this.duration = Math.max(0, duration);
        this.targetValue = MathHelper.clamp(targetValue, 0f, 1f);
    }

    public static FadeBlackS2CPacket toBlack(int duration) {
        return new FadeBlackS2CPacket(duration, 1f);
    }

    public static FadeBlackS2CPacket fromBlack(int duration) {
        return new FadeBlackS2CPacket(duration, 0f);
    }

    public static FadeBlackS2CPacket instant(float newValue) {
        return new FadeBlackS2CPacket(0, newValue);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static FadeBlackS2CPacket read(PacketByteBuf buf) {
        return new FadeBlackS2CPacket(buf.readVarInt(), buf.readFloat());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(duration);
        buf.writeFloat(targetValue);
    }

    public void send(Collection<ServerPlayerEntity> targets) {
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, this);
        }
    }
}
