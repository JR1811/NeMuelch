package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ReboundEffect extends StatusEffect {
    public ReboundEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        MiscEntityComponent component = MiscEntityComponent.get(entity);
        component.startRebound();
    }

    public record DamageInstance(DamageSource source, float damage) {
        @Nullable
        public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
            NbtCompound nbt = new NbtCompound();
            DynamicRegistryManager dynamicRegistries = (DynamicRegistryManager) registries;
            Identifier damageTypeId = dynamicRegistries
                    .get(RegistryKeys.DAMAGE_TYPE)
                    .getId(source.getTypeRegistryEntry().value());
            if (damageTypeId == null) {
                return null;
            }

            nbt.putString("damage_type", damageTypeId.toString());
            nbt.putFloat("damage", damage);
            return nbt;
        }

        public static DamageInstance fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            Identifier damageTypeId = Identifier.tryParse(nbt.getString("damage_type"));
            float damage = nbt.getFloat("damage");
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
}
