package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchFluids;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchTags;

public class NeMuelchTagsGenerators {
    public static class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
        public ItemTagProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateTags() {
            getOrCreateTagBuilder(NeMuelchTags.Items.ARKADUSCANE_PROJECTILE)
                    .add(Items.GOLD_NUGGET, Items.IRON_NUGGET);
            getOrCreateTagBuilder(NeMuelchTags.Items.CAMPFIRE_IGNITER)
                    .add(Items.TORCH);
            getOrCreateTagBuilder(NeMuelchTags.Items.CAMPFIRE_IGNITER)
                    .add(Items.TORCH);
            getOrCreateTagBuilder(NeMuelchTags.Items.GLOVES)
                    .add(NeMuelchItems.GLOVE);
            getOrCreateTagBuilder(NeMuelchTags.Items.IGNITES_POTS)
                    .add(Items.TORCH, Items.REDSTONE_TORCH, Items.FLINT_AND_STEEL);
            getOrCreateTagBuilder(NeMuelchTags.Items.SHIELD_REPAIR_MATERIAL)
                    .add(Items.IRON_INGOT);
            getOrCreateTagBuilder(NeMuelchTags.Items.PESTCANE_UPGRADE_MATERIAL)
                    .add(Items.EMERALD, Items.LAPIS_LAZULI, Items.IRON_INGOT, Items.GOLD_INGOT, Items.AMETHYST_SHARD);
            getOrCreateTagBuilder(NeMuelchTags.Items.PULL_BODY_TOOLS)
                    .addOptionalTag(NeMuelchTags.Items.PESTCANES);
            getOrCreateTagBuilder(NeMuelchTags.Items.ROPER_ROPES)
                    .add(NeMuelchBlocks.ROPE.asItem());


            NeMuelchItems.NEMUELCH_DRINKS.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.NEMUELCH_DRINKS).add(item)
            );
            NeMuelchItems.PEST_CANES.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.PESTCANES).add(item)
            );
        }
    }

    public static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
        public BlockTagProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateTags() {
            getOrCreateTagBuilder(NeMuelchTags.Blocks.HEAT_EMITTING_BLOCKS)
                    .add(Blocks.MAGMA_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.SHROOMLIGHT,
                            Blocks.GLOWSTONE, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.TORCH_IGNITING_BLOCKS)
                    .add(Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.TORCH, Blocks.WALL_TORCH,
                            Blocks.SOUL_CAMPFIRE, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
                            Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.LAVA, Blocks.LAVA_CAULDRON, Blocks.LAVA);
            getOrCreateTagBuilder(BlockTags.CLIMBABLE)
                    .add(NeMuelchBlocks.IRON_SCAFFOLDING, NeMuelchBlocks.ROPE, NeMuelchBlocks.ROPER);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)
                    .add(Blocks.CHEST, Blocks.BARREL)
                    .addOptionalTag(BlockTags.DOORS)
                    .addOptionalTag(BlockTags.TRAPDOORS);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.LIFT_ROPE_ANCHOR)
                    .add(Blocks.GRINDSTONE, Blocks.IRON_BARS)
                    .addOptionalTag(BlockTags.FENCES);
        }
    }

    public static class FluidTagProvider extends FabricTagProvider.FluidTagProvider {
        public FluidTagProvider(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateTags() {
            getOrCreateTagBuilder(FluidTags.WATER)
                    .add(NeMuelchFluids.HONEY_STILL, NeMuelchFluids.HONEY_FLOWING,
                            NeMuelchFluids.SLIME_STILL, NeMuelchFluids.SLIME_FLOWING);
        }
    }

    public static void registerAll(FabricDataGenerator generator) {
        generator.addProvider(ItemTagProvider::new);
        generator.addProvider(BlockTagProvider::new);
        generator.addProvider(FluidTagProvider::new);
    }
}
