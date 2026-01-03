package net.shirojr.nemuelch.compat.satin.util;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.StringIdentifiable;
import net.shirojr.nemuelch.compat.timewind.Phase;

import java.util.Locale;

public enum NetworkingParameter implements StringIdentifiable {
    INTENSITY(NbtElement.FLOAT_TYPE),
    CLAMP_1(NbtElement.FLOAT_TYPE),
    CLAMP_2(NbtElement.FLOAT_TYPE),
    TARGET(NbtElement.FLOAT_TYPE),
    CURRENT(NbtElement.FLOAT_TYPE);

    public static final com.mojang.serialization.Codec<NetworkingParameter> CODEC = StringIdentifiable.createCodec(NetworkingParameter::values);
    private final int nbtElementType;

    NetworkingParameter(int nbtElementType) {
        this.nbtElementType = nbtElementType;
    }

    /**
     * @see net.minecraft.nbt.NbtElement NbtElement
     */
    public int getNbtElementType() {
        return nbtElementType;
    }

    @Override
    public String asString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
