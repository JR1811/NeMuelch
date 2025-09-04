package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.shirojr.nemuelch.block.custom.ChimneyBlock;
import net.shirojr.nemuelch.block.custom.HalfSlabBlock;
import net.shirojr.nemuelch.block.custom.PlateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlocks;

public class NeMuelchLootTableGenerator {
    public static class BlockLootGenerator extends FabricBlockLootTableProvider {
        protected BlockLootGenerator(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            addDrop(NeMuelchBlocks.DROP_POT, NeMuelchBlocks.DROP_POT);
            addDrop(NeMuelchBlocks.PESTCANE_STATION, NeMuelchBlocks.PESTCANE_STATION);

            for (Block entry : NeMuelchBlocks.FOG_BLOCKS) {
                addDrop(entry, entry);
            }

            for (ChimneyBlock chimneyBlock : NeMuelchBlocks.CHIMNEYS) {
                addDrop(chimneyBlock, block -> drops(chimneyBlock.getVariant().parentBlock(), UniformLootNumberProvider.create(3.0f, 6.0f)));
            }
            for (PlateBlock plateBlock : NeMuelchBlocks.PLATES) {
                addDrop(plateBlock, block -> drops(plateBlock.getVariant().parentBlock(), UniformLootNumberProvider.create(1.0f, 2.0f)));
            }
            for (HalfSlabBlock halfSlabBlock : NeMuelchBlocks.HALF_SLABS) {
                addDrop(halfSlabBlock, block -> drops(halfSlabBlock.getVariant().parentBlock(), UniformLootNumberProvider.create(2.0f, 3.0f)));
            }
        }
    }

    public static void registerAll(FabricDataGenerator.Pack generator) {
        generator.addProvider(BlockLootGenerator::new);
    }
}
