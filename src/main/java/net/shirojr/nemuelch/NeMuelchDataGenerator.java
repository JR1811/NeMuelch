package net.shirojr.nemuelch;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.shirojr.nemuelch.datagen.*;
import net.shirojr.nemuelch.init.NeMuelchBiomes;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchDimensions;
import net.shirojr.nemuelch.init.NeMuelchWorldGen;

public class NeMuelchDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(NeMuelchModelGenerator::new);
        pack.addProvider(NeMuelchRecipeGenerator::new);
        pack.addProvider(NemuelchTranslationGenerator::new);
        pack.addProvider(NeMuelchWorldGenerator::new);
        pack.addProvider(NeMuelchDynamicRegistriesGenerator::new);
        pack.addProvider(NeMuelchSoundsFileGenerator::new);

        NeMuelchLootTableGenerator.registerAll(pack);
        NeMuelchTagsGenerators.registerAll(pack);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, NeMuelchWorldGen.ConfiguredFeatures::bootstrap);
        builder.addRegistry(RegistryKeys.DAMAGE_TYPE, NeMuelchDamageTypes::bootstrap);
        builder.addRegistry(RegistryKeys.BIOME, NeMuelchBiomes::bootstrap);
        builder.addRegistry(RegistryKeys.DIMENSION_TYPE, NeMuelchDimensions::bootstrap);
    }
}
