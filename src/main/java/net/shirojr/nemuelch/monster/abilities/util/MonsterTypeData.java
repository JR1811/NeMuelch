package net.shirojr.nemuelch.monster.abilities.util;

import net.minecraft.nbt.NbtCompound;

public interface MonsterTypeData {
    void toNbt(NbtCompound nbt);

    void fromNbt(NbtCompound nbt);
}
