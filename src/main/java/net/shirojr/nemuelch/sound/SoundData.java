package net.shirojr.nemuelch.sound;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@SuppressWarnings("unused")
public record SoundData(SoundEvent sound, SoundCategory category, float volume, float pitch, int repeat) {
    public void toNbt(NbtCompound nbt, String key) {
        NbtCompound entryNbt = new NbtCompound();
        entryNbt.putString("sound", this.sound.getId().toString());
        entryNbt.putInt("category", this.category.ordinal());
        entryNbt.putFloat("volume", this.volume);
        entryNbt.putFloat("pitch", this.pitch);
        entryNbt.putInt("repeat", this.repeat);
        nbt.put(key, entryNbt);
    }

    public static SoundData fromNbt(NbtCompound nbt, String key) {
        NbtCompound entryNbt = nbt.getCompound(key);
        SoundEvent sound = SoundEvent.of(Identifier.tryParse(entryNbt.getString("sound")));
        SoundCategory category = SoundCategory.values()[nbt.getInt("category")];
        float volume = nbt.getFloat("volume");
        float pitch = nbt.getFloat("pitch");
        int repeat = nbt.getInt("repeat");
        return new SoundData(sound, category, volume, pitch, repeat);
    }

    public void toPacketByteBuf(PacketByteBuf buf) {
        buf.writeString(sound.getId().toString());
        buf.writeVarInt(category.ordinal());
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeVarInt(repeat);
    }

    public static SoundData fromPacketByteBuf(PacketByteBuf buf) {
        SoundEvent sound = SoundEvent.of(Identifier.tryParse(buf.readString()));
        SoundCategory category = SoundCategory.values()[buf.readVarInt()];
        float volume = buf.readFloat();
        float pitch = buf.readFloat();
        int repeat = buf.readVarInt();
        return new SoundData(sound, category, volume, pitch, repeat);
    }
}
