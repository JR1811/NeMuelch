package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Ability {
    default void tickServer(ServerPlayerEntity player) {
    }

    void fromNbt(NbtCompound nbt);

    void toNbt(NbtCompound nbt);
}
