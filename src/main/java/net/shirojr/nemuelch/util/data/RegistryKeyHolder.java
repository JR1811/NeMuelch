package net.shirojr.nemuelch.util.data;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public record RegistryKeyHolder<T>(RegistryKey<T> key, T value) {
    @SuppressWarnings("unused")
    public Identifier getEntryId() {
        return key.getValue();
    }
}
