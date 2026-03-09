package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;

import java.util.concurrent.CompletableFuture;

public class NeMuelchDynamicRegistriesGenerator extends FabricDynamicRegistryProvider {
    public NeMuelchDynamicRegistriesGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        for (var entry : NeMuelchDamageTypes.ALL_DAMAGE_TYPES.entrySet()) {
            entries.add(registries.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE), entry.getValue().get());
        }
    }

    @Override
    public String getName() {
        return "NeMuelch Dynamic Registries Data Generator";
    }
}
