package net.shirojr.nemuelch.network.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public record MonsterAbilityKeyPressC2SPacket(int index, boolean pressed) implements FabricPacket {
    public static final Identifier KEY = NeMuelch.getId("montser_ability_key_press");
    public static final PacketType<MonsterAbilityKeyPressC2SPacket> TYPE = PacketType.create(KEY, MonsterAbilityKeyPressC2SPacket::read);

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    private static MonsterAbilityKeyPressC2SPacket read(PacketByteBuf buf) {
        return new MonsterAbilityKeyPressC2SPacket(buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.index);
        buf.writeBoolean(this.pressed);
    }

    public void send() {
        ClientPlayNetworking.send(this);
    }
}
