package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetLoreLootFunction;
import net.minecraft.loot.function.SetNameLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.util.VariationHolder;
import net.shirojr.nemuelch.compat.cca.component.RottenMeatDigestionComponent;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchItems;

import java.util.Set;
import java.util.function.BiConsumer;

public class NeMuelchLootTableGenerator {
    public static class BlockLootGenerator extends FabricBlockLootTableProvider {
        protected BlockLootGenerator(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generate() {
            addDrop(NeMuelchBlocks.DROP_POT, NeMuelchBlocks.DROP_POT);
            addDrop(NeMuelchBlocks.PESTCANE_STATION, NeMuelchBlocks.PESTCANE_STATION);
            addDrop(NeMuelchBlocks.ROTTEN_TREE_LOG);
            addDrop(NeMuelchBlocks.ROTTEN_TREE_SAPLING);
            addDrop(NeMuelchBlocks.ROTTEN_MEAT);

            for (Block entry : NeMuelchBlocks.FOG_BLOCKS) {
                addDrop(entry, entry);
            }

            for (VariationHolder variationBlock : NeMuelchBlocks.VARIATION_BLOCKS) {
                addDrop(variationBlock.getBlock());
            }

            for (CrateBlock crate : NeMuelchBlocks.CRATES) {
                addDrop(crate);
            }
        }

    }

    public static class CustomLootGenerator extends SimpleFabricLootTableProvider {
        public CustomLootGenerator(FabricDataOutput output) {
            super(output, LootContextTypes.CHEST);
        }

        @Override
        public void accept(BiConsumer<Identifier, LootTable.Builder> exporter) {
            exporter.accept(NeMuelch.getId(RottenMeatDigestionComponent.LOOT_TABLE_FILE_BASE_NAME + "_high"),
                    LootTable.builder()
                            .pool(
                                    LootPool.builder().rolls(UniformLootNumberProvider.create(2f, 4f))
                                            .with(ItemEntry.builder(NeMuelchItems.ROTTEN_MEAT_LUMP).weight(2))
                                            .with(ItemEntry.builder(NeMuelchItems.MEAT_LUMP).weight(5))
                                            .with(EmptyEntry.builder().weight(1))
                            )
                            .pool(
                                    appendLoreItems(
                                            Set.of(
                                                    new WeightedLoreEntry(Items.BONE, 2, 1),
                                                    new WeightedLoreEntry(Items.ROTTEN_FLESH, 2, 2),
                                                    new WeightedLoreEntry(Items.BONE, 1, 3),
                                                    new WeightedLoreEntry(NeMuelchItems.ROTTEN_MEAT_LUMP, 1, 4),
                                                    new WeightedLoreEntry(Items.ROTTEN_FLESH, 1, 5),
                                                    new WeightedLoreEntry(Items.ROTTEN_FLESH, 2, 6),
                                                    new WeightedLoreEntry(Items.DEAD_BUSH, 3, 7),
                                                    new WeightedLoreEntry(NeMuelchItems.ROTTEN_MEAT_LUMP, 5, 8),
                                                    new WeightedLoreEntry(NeMuelchItems.MEAT_LUMP, 2, 9),
                                                    new WeightedLoreEntry(NeMuelchItems.ROTTEN_MEAT_LUMP, 1, 10)
                                            ),
                                            LootPool.builder().rolls(ConstantLootNumberProvider.create(2f))
                                                    .with(ItemEntry.builder(Items.ROTTEN_FLESH).weight(10))
                                                    .with(ItemEntry.builder(Items.DEAD_BUSH).weight(4))
                                                    .with(ItemEntry.builder(Items.BONE).weight(4))
                                                    .with(EmptyEntry.builder().weight(4)))
                            )
            );


            exporter.accept(NeMuelch.getId(RottenMeatDigestionComponent.LOOT_TABLE_FILE_BASE_NAME + "_mid"),
                    LootTable.builder()
                            .pool(
                                    LootPool.builder().rolls(UniformLootNumberProvider.create(4f, 8f))
                                            .with(ItemEntry.builder(NeMuelchItems.ROTTEN_MEAT_LUMP).weight(2))
                                            .with(ItemEntry.builder(NeMuelchItems.MEAT_LUMP).weight(5))
                                            .with(EmptyEntry.builder().weight(2))
                            )
            );


            exporter.accept(NeMuelch.getId(RottenMeatDigestionComponent.LOOT_TABLE_FILE_BASE_NAME + "_low"),
                    LootTable.builder()
                            .pool(
                                    LootPool.builder().rolls(UniformLootNumberProvider.create(4f, 8f))
                                            .with(ItemEntry.builder(NeMuelchItems.ROTTEN_MEAT_LUMP).weight(5))
                                            .with(ItemEntry.builder(NeMuelchItems.MEAT_LUMP).weight(2))
                                            .with(EmptyEntry.builder().weight(3))
                            )
                            .pool(
                                    LootPool.builder().rolls(ConstantLootNumberProvider.create(3f))
                                            .with(ItemEntry.builder(Items.ROTTEN_FLESH).weight(2))
                                            .with(EmptyEntry.builder().weight(1))
                            )
            );
        }

        private static LootPool.Builder appendLoreItems(Set<WeightedLoreEntry> entries, LootPool.Builder builder) {
            for (WeightedLoreEntry entry : entries) {
                builder = builder.with(ItemEntry.builder(entry.item).weight(entry.weight)
                        .apply(SetNameLootFunction.builder(Text.translatable("lore.nemuelch.rotten_meat.info")))
                        .apply(SetLoreLootFunction.builder().lore(
                                Text.translatable("lore.nemuelch.rotten_meat." + entry.translationIndex)
                        ))
                );
            }
            return builder;
        }
    }

    public static void registerAll(FabricDataGenerator.Pack generator) {
        generator.addProvider(BlockLootGenerator::new);
        generator.addProvider(CustomLootGenerator::new);
    }

    public record WeightedLoreEntry(ItemConvertible item, int weight, int translationIndex) {
    }
}
