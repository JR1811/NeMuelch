package net.shirojr.nemuelch.util.wrapper;

import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.NeMuelch;

import java.util.function.Function;

public interface ICustomPlayerEntityData {
    String CUSTOM_PLAYER_DATA_NBT_KEY = NeMuelch.MOD_ID + ".customPlayerData";

    /**
     * Used to retrieve custom NBT Data from PlayerEntities
     *
     * @return custom NBT data from PlayerEntity
     */
    NbtCompound nemuelch$getPersistentData();

    /**
     * Used to edit custom NBT Data from PlayerEntities
     *
     * @param action Use a lambda to modify the custom data
     */
    <T> T nemuelch$editPersistentData(Function<NbtCompound, T> action);

    record Wrapper(NbtCompound nbt) {
    }
}
