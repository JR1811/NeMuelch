package net.shirojr.nemuelch;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.shirojr.nemuelch.datagen.NeMuelchModelGenerator;
import net.shirojr.nemuelch.datagen.NeMuelchTagsGenerators;

public class NeMuelchDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(NeMuelchModelGenerator::new);
        NeMuelchTagsGenerators.registerAll(fabricDataGenerator);
    }
}
