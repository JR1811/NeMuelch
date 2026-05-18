package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.shirojr.nemuelch.init.NeMuelchBiomes;
import net.shirojr.nemuelch.init.NeMuelchDimensions;
import net.shirojr.nemuelch.util.data.RegistryKeyHolder;

import java.util.concurrent.CompletableFuture;

public class NeMuelchWorldGenerator extends FabricDynamicRegistryProvider {
    public NeMuelchWorldGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getWrapperOrThrow(RegistryKeys.CONFIGURED_FEATURE));
        for (RegistryKeyHolder<Biome> entry : NeMuelchBiomes.ALL) {
            entries.add(entry.key(), entry.value());
        }

        for (RegistryKeyHolder<DimensionType> entry : NeMuelchDimensions.ALL) {
            entries.add(entry.key(), entry.value());
        }
    }

    @Override
    public String getName() {
        return "NeMuelch World Gen";
    }
}
