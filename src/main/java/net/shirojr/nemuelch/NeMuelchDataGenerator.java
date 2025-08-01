package net.shirojr.nemuelch;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.shirojr.nemuelch.datagen.*;

public class NeMuelchDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(NeMuelchModelGenerator::new);
        pack.addProvider(NeMuelchRecipeGenerator::new);
        pack.addProvider(NemuelchTranslationGenerator::new);

        NeMuelchLootTableGenerator.registerAll(pack);
        NeMuelchTagsGenerators.registerAll(pack);
    }
}
