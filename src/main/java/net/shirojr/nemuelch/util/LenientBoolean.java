package net.shirojr.nemuelch.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

@SuppressWarnings("unused")
public enum LenientBoolean {
    TRUE,
    FALSE,
    NONE;

    public boolean asStrict() {
        return this == TRUE;
    }

    public static LenientBoolean fromPacketByteBuf(PacketByteBuf buf) {
        byte b = buf.readByte();
        return LenientBoolean.values()[b];
    }

    public void toPacketByteBuf(PacketByteBuf buf) {
        buf.writeByte(this.ordinal());
    }

    public static LenientBoolean fromNbt(NbtCompound nbt, String key) {
        return LenientBoolean.values()[nbt.getByte(key)];
    }

    public void toNbt(NbtCompound nbt, String key) {
        nbt.putByte(key, (byte) this.ordinal());
    }
}
