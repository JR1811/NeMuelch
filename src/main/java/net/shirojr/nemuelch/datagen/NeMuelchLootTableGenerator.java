package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

public class NeMuelchLootTableGenerator {
    public static class BlockLootGenerator extends FabricBlockLootTableProvider {
        public BlockLootGenerator(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateBlockLootTables() {
            addDrop(NeMuelchBlocks.DROP_POT, NeMuelchBlocks.DROP_POT);
            addDrop(NeMuelchBlocks.PESTCANE_STATION, NeMuelchBlocks.PESTCANE_STATION);

            for (Block entry : NeMuelchBlocks.FOG_BLOCKS) {
                addDrop(entry, entry);
            }
        }
    }

    public static void registerAll(FabricDataGenerator generator) {
        generator.addProvider(BlockLootGenerator::new);
    }
}
