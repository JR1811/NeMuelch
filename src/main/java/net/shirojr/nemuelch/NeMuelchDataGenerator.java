package net.shirojr.nemuelch;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.shirojr.nemuelch.datagen.*;

public class NeMuelchDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(NeMuelchModelGenerator::new);
        fabricDataGenerator.addProvider(NeMuelchRecipeGenerator::new);
        fabricDataGenerator.addProvider(NemuelchTranslationGenerator::new);

        NeMuelchLootTableGenerator.registerAll(fabricDataGenerator);
        NeMuelchTagsGenerators.registerAll(fabricDataGenerator);
    }
}
