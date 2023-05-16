package net.shirojr.nemuelch.util.cast;

import net.minecraft.nbt.NbtCompound;
import org.apache.http.annotation.Obsolete;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IBodyPartSaver {
    NbtCompound getPersistentData();

    <T> T editPersistentData(Function<NbtCompound, T> action);

    record Wrapper(NbtCompound nbt) {}
}
