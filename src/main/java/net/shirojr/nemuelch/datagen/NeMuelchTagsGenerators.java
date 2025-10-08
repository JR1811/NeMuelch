package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.shirojr.nemuelch.block.util.VariationHolder;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchTags;

import java.util.concurrent.CompletableFuture;

public class NeMuelchTagsGenerators {
    public static class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
        public ItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(NeMuelchTags.Items.ARKADUSCANE_PROJECTILE)
                    .add(Items.GOLD_NUGGET, Items.IRON_NUGGET);
            getOrCreateTagBuilder(NeMuelchTags.Items.CAMPFIRE_IGNITER)
                    .add(Items.TORCH);
            getOrCreateTagBuilder(NeMuelchTags.Items.CAMPFIRE_IGNITER)
                    .add(Items.TORCH);
            getOrCreateTagBuilder(NeMuelchTags.Items.GLOVES)
                    .add(NeMuelchItems.GLOVE);
            getOrCreateTagBuilder(NeMuelchTags.Items.IGNITES_POTS)
                    .add(Items.REDSTONE_TORCH, Items.FLINT_AND_STEEL);
            getOrCreateTagBuilder(NeMuelchTags.Items.SHIELD_REPAIR_MATERIAL)
                    .add(Items.IRON_INGOT);
            getOrCreateTagBuilder(NeMuelchTags.Items.PESTCANE_UPGRADE_MATERIAL)
                    .add(Items.EMERALD, Items.LAPIS_LAZULI, Items.IRON_INGOT, Items.GOLD_INGOT, Items.AMETHYST_SHARD);
            getOrCreateTagBuilder(NeMuelchTags.Items.PULL_BODY_TOOLS)
                    .addOptionalTag(NeMuelchTags.Items.PESTCANES);
            getOrCreateTagBuilder(NeMuelchTags.Items.ROPER_ROPES)
                    .add(NeMuelchBlocks.ROPE.asItem());
            getOrCreateTagBuilder(NeMuelchTags.Items.BOOK_WRAPPER_CONTENT)
                    .add(Items.BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK, Items.KNOWLEDGE_BOOK, Items.WRITTEN_BOOK);

            NeMuelchItems.NEMUELCH_DRINKS.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.NEMUELCH_DRINKS).add(item)
            );
            NeMuelchItems.PEST_CANES.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.PESTCANES).add(item)
            );
        }
    }

    public static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
        public BlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
            getOrCreateTagBuilder(NeMuelchTags.Blocks.HEAT_EMITTING_BLOCKS)
                    .add(Blocks.MAGMA_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.SHROOMLIGHT,
                            Blocks.GLOWSTONE, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.TORCH_IGNITING_BLOCKS)
                    .add(Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.TORCH, Blocks.WALL_TORCH,
                            Blocks.SOUL_CAMPFIRE, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
                            Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.LAVA_CAULDRON,
                            Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER)
                    .addOptionalTag(BlockTags.CANDLES)
            ;
            getOrCreateTagBuilder(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)
                    .add(Blocks.CHEST, Blocks.BARREL)
                    .addOptionalTag(BlockTags.DOORS)
                    .addOptionalTag(BlockTags.TRAPDOORS)
                    .addOptionalTag(ConventionalBlockTags.CHESTS)
                    .addOptionalTag(ConventionalBlockTags.WOODEN_BARRELS)
                    .addOptionalTag(ConventionalBlockTags.BOOKSHELVES);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.LIFT_ROPE_ANCHOR)
                    .add(Blocks.GRINDSTONE, Blocks.IRON_BARS)
                    .addOptionalTag(BlockTags.FENCES);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.FERTILIZABLE_WHITELIST)
                    .addOptionalTag(BlockTags.SAPLINGS);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.NEVER_BLIGHT)
                    .addOptionalTag(BlockTags.WITHER_IMMUNE);

            getOrCreateTagBuilder(NeMuelchTags.Blocks.SIGIL_COLOR_BLOCKS).addOptionalTag(BlockTags.CANDLES);

            getOrCreateTagBuilder(BlockTags.CLIMBABLE).add(NeMuelchBlocks.IRON_SCAFFOLDING, NeMuelchBlocks.ROPE, NeMuelchBlocks.ROPER);

            for (VariationHolder variationHolder : NeMuelchBlocks.CHIMNEYS) {
                getOrCreateTagBuilder(BlockTags.CLIMBABLE).add(variationHolder.getBlock());
            }
            for (VariationHolder variationHolder : NeMuelchBlocks.DOUBLE_PLATES) {
                getOrCreateTagBuilder(BlockTags.CLIMBABLE).add(variationHolder.getBlock());
            }
            for (VariationHolder variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
                for (TagKey<Block> blockTag : variationHolder.getVariant().blockTags()) {
                    getOrCreateTagBuilder(blockTag).add(variationHolder.getBlock());
                }
            }
        }
    }

    public static class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
        public EntityTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(NeMuelchTags.EntityTypes.VAMPIRE_INDIGESTIBLE).add(
                    EntityType.ALLAY, EntityType.VEX,
                    EntityType.SHULKER, EntityType.SLIME
            ).addOptionalTag(EntityTypeTags.SKELETONS);
        }
    }

    public static void registerAll(FabricDataGenerator.Pack generator) {
        generator.addProvider(ItemTagProvider::new);
        generator.addProvider(BlockTagProvider::new);
        generator.addProvider(EntityTypeTagProvider::new);
    }
}
