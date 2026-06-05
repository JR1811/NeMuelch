package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.shirojr.nemuelch.init.NeMuelchBiomes;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchDimensions;
import net.shirojr.nemuelch.util.data.RegistryKeyHolder;

import java.util.concurrent.CompletableFuture;

public class NeMuelchDynamicRegistriesGenerator extends FabricDynamicRegistryProvider {
    public NeMuelchDynamicRegistriesGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        for (var entry : NeMuelchDamageTypes.ALL.entrySet()) {
            entries.add(registries.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE), entry.getValue().get());
        }
        for (RegistryKeyHolder<Biome> holder : NeMuelchBiomes.ALL) {
            entries.add(registries.getWrapperOrThrow(RegistryKeys.BIOME), holder.key());
        }
        for (RegistryKeyHolder<DimensionType> holder : NeMuelchDimensions.ALL) {
            entries.add(registries.getWrapperOrThrow(RegistryKeys.DIMENSION_TYPE), holder.key());
        }
    }

    @Override
    public String getName() {
        return "NeMuelch Dynamic Registries Data Generator";
    }
}
