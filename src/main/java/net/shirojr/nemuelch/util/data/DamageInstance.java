package net.shirojr.nemuelch.util.data;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record DamageInstance(DamageSource source, float damage) {
    @Nullable
    public NbtCompound createNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        DynamicRegistryManager dynamicRegistries = (DynamicRegistryManager) registries;
        Identifier damageTypeId = dynamicRegistries
                .get(RegistryKeys.DAMAGE_TYPE)
                .getId(source.getTypeRegistryEntry().value());
        if (damageTypeId == null) {
            return null;
        }

        nbt.putString(NeMuelchNbtKeys.DAMAGE_TYPE, damageTypeId.toString());
        nbt.putFloat(NeMuelchNbtKeys.DAMAGE, damage);
        return nbt;
    }

    public static DamageInstance fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Identifier damageTypeId = Identifier.tryParse(nbt.getString(NeMuelchNbtKeys.DAMAGE_TYPE));
        float damage = nbt.getFloat(NeMuelchNbtKeys.DAMAGE);
        RegistryWrapper.Impl<DamageType> damageTypeRegistry = registries.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);
        RegistryEntry<DamageType> damageTypeEntry = damageTypeRegistry
                .getOptional(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, damageTypeId))
                .orElseThrow();
        DamageSource source = new DamageSource(damageTypeEntry);
        return new DamageInstance(source, damage);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DamageInstance other)) return false;
        return damage() == other.damage();
    }

    @Override
    public int hashCode() {
        return Objects.hash(source(), damage());
    }
}