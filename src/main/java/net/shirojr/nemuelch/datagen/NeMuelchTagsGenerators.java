package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.shirojr.nemuelch.init.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NeMuelchTagsGenerators {
    public static void registerAll(FabricDataGenerator.Pack generator) {
        generator.addProvider(ItemTagProvider::new);
        generator.addProvider(BlockTagProvider::new);
        generator.addProvider(EntityTypeTagProvider::new);
        generator.addProvider(DamageTypeTagsProvider::new);
        generator.addProvider(BiomeTagProvider::new);
        generator.addProvider(DimensionTypeTagProvider::new);
        generator.addProvider(FluidTagProvider::new);
    }

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
                    .add(Items.BOOK, Items.ENCHANTED_BOOK, Items.WRITABLE_BOOK, Items.KNOWLEDGE_BOOK,
                            Items.WRITTEN_BOOK, NeMuelchItems.BLOCKED_BOOK_ARTIFACT);
            getOrCreateTagBuilder(NeMuelchTags.Items.NO_FOOD_STACK_DECREMENT).add(NeMuelchItems.ROTTEN_MEAT_APPLE);
            getOrCreateTagBuilder(NeMuelchTags.Items.NEVER_BLIGHT);
            getOrCreateTagBuilder(NeMuelchTags.Items.BLOCK_THIRD_PERSON_RENDERING).add(NeMuelchItems.ENTITY_TRANSPORTER, NeMuelchItems.REFILLER, NeMuelchItems.SOUND_TOOL);
            getOrCreateTagBuilder(NeMuelchTags.Items.CRATE_STANDS).add(Items.STICK).addOptionalTag(ItemTags.WOODEN_FENCES);

            NeMuelchItems.NEMUELCH_DRINKS.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.NEMUELCH_DRINKS).add(item)
            );
            NeMuelchItems.PEST_CANES.forEach(item ->
                    getOrCreateTagBuilder(NeMuelchTags.Items.PESTCANES).add(item)
            );
            NeMuelchItems.CRATES.forEach(crateItem -> getOrCreateTagBuilder(NeMuelchTags.Items.CRATES).add(crateItem));

            getOrCreateTagBuilder(NeMuelchTags.Items.STRIPPED_LOGS)
                    .add(
                            Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_DARK_OAK_LOG,
                            Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_MANGROVE_LOG,
                            Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_WARPED_STEM
                    );

            getOrCreateTagBuilder(NeMuelchTags.Items.SOAP).add(NeMuelchItems.SOAP, NeMuelchItems.CREATIVE_SOAP);

            getOrCreateTagBuilder(NeMuelchTags.Items.DUMMY_CLEAR).addOptionalTag(NeMuelchTags.Items.SOAP);
            getOrCreateTagBuilder(NeMuelchTags.Items.DUMMY_UNDEAD).add(Items.ROTTEN_FLESH, Items.POISONOUS_POTATO, Items.ZOMBIE_HEAD, Items.ZOMBIE_SPAWN_EGG, Items.HUSK_SPAWN_EGG, Items.ZOMBIE_HORSE_SPAWN_EGG);
            getOrCreateTagBuilder(NeMuelchTags.Items.DUMMY_ARTHROPOD).add(Items.SPIDER_EYE, Items.FERMENTED_SPIDER_EYE, Items.STRING, Items.SPIDER_SPAWN_EGG);
            getOrCreateTagBuilder(NeMuelchTags.Items.DUMMY_ILLAGER).add(Items.GLOWSTONE_DUST);
            getOrCreateTagBuilder(NeMuelchTags.Items.DUMMY_AQUATIC).addOptionalTag(ItemTags.FISHES).add(Items.KELP);

            getOrCreateTagBuilder(ItemTags.AXES).add(NeMuelchItems.CHAINED_MACE);

            NeMuelchItems.SHIELDS.forEach(item -> getOrCreateTagBuilder(ConventionalItemTags.SHIELDS).add(item));
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
                    .addOptionalTag(BlockTags.CANDLES);
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

            getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                    .add(NeMuelchBlocks.ROTTEN_TREE_LOG)
                    .add(NeMuelchBlocks.WATER_CRATE)
                    .addOptionalTag(NeMuelchTags.Blocks.CRATES);
            getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(NeMuelchBlocks.ROTTEN_MEAT);
            getOrCreateTagBuilder(BlockTags.LOGS).add(NeMuelchBlocks.ROTTEN_TREE_LOG);
            getOrCreateTagBuilder(BlockTags.SAPLINGS).add(NeMuelchBlocks.ROTTEN_TREE_SAPLING);

            getOrCreateTagBuilder(NeMuelchTags.Blocks.IGNORED_BY_SHOVEL_FLATTENING);

            getOrCreateTagBuilder(NeMuelchTags.Blocks.DEEP_WATER_INCLUSIVE)
                    .add(Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.SEA_PICKLE, Blocks.BUBBLE_COLUMN)
                    .addOptional(Identifier.tryParse("rocks:starfish"))
                    .addOptional(Identifier.tryParse("rocks:seashell"))
                    .addOptional(Identifier.tryParse("hybrid-aquatic:sargassum"))
                    .addOptional(Identifier.tryParse("hybrid-aquatic:floating_sargassum"))
                    .addOptional(Identifier.tryParse("hybrid-aquatic:water_lettuce"));

            NeMuelchBlocks.CRATES.forEach(crateBlock -> getOrCreateTagBuilder(NeMuelchTags.Blocks.CRATES).add(crateBlock));

            getOrCreateTagBuilder(NeMuelchTags.Blocks.LANTERNS).add(NeMuelchBlocks.WALL_LANTERN);
            getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(NeMuelchBlocks.WALL_LANTERN);

            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_BLACKLIST).add(Blocks.BEDROCK);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_DEATH).add(Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_BURN).add(Blocks.MAGMA_BLOCK);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_HUNGER).add(NeMuelchBlocks.ROTTEN_TREE_LOG);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_POISON).add(NeMuelchBlocks.ROTTEN_MEAT);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_WITHER).add(Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
            getOrCreateTagBuilder(NeMuelchTags.Blocks.CHAINED_MACE_SLIME).add(Blocks.SLIME_BLOCK, Blocks.HONEY_BLOCK);

            getOrCreateTagBuilder(NeMuelchTags.Blocks.TERRAFORM_PREPARATION)
                    .add(Blocks.WATER, Blocks.LAVA, Blocks.SEAGRASS, Blocks.BAMBOO, Blocks.SEA_PICKLE,
                            Blocks.PUMPKIN, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM,
                            Blocks.MELON, Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM)
                    .addOptionalTag(BlockTags.LEAVES)
                    .addOptionalTag(BlockTags.LOGS)
                    .addOptionalTag(BlockTags.FLOWERS)
                    .addOptionalTag(BlockTags.TALL_FLOWERS)
                    .addOptionalTag(BlockTags.REPLACEABLE)
                    .addOptionalTag(ConventionalBlockTags.BUDS)
                    .addOptionalTag(ConventionalBlockTags.CLUSTERS)
                    .addOptional(Identifier.of("twigs", "twig"))
                    .addOptional(Identifier.of("rocks", "rock"))
                    .addOptional(Identifier.of("rocks", "granite_rock"))
                    .addOptional(Identifier.of("rocks", "diorite_rock"))
                    .addOptional(Identifier.of("rocks", "andesite_rock"))
                    .addOptional(Identifier.of("rocks", "sand_rock"))
                    .addOptional(Identifier.of("rocks", "red_sand_rock"))
                    .addOptional(Identifier.of("rocks", "gravel_rock"))
                    .addOptional(Identifier.of("rocks", "end_stone_rock"))
                    .addOptional(Identifier.of("rocks", "netherrack_rock"))
                    .addOptional(Identifier.of("rocks", "soul_soil_rock"))
                    .addOptional(Identifier.of("rocks", "oak_stick"))
                    .addOptional(Identifier.of("rocks", "spruce_stick"))
                    .addOptional(Identifier.of("rocks", "birch_stick"))
                    .addOptional(Identifier.of("rocks", "acacia_stick"))
                    .addOptional(Identifier.of("rocks", "jungle_stick"))
                    .addOptional(Identifier.of("rocks", "dark_oak_stick"))
                    .addOptional(Identifier.of("rocks", "mangrove_stick"))
                    .addOptional(Identifier.of("rocks", "cherry_stick"))
                    .addOptional(Identifier.of("rocks", "bamboo_stick"))
                    .addOptional(Identifier.of("rocks", "crimson_stick"))
                    .addOptional(Identifier.of("rocks", "warped_stick"))
                    .addOptional(Identifier.of("rocks", "pinecone"))
                    .addOptional(Identifier.of("rocks", "seashell"))
                    .addOptional(Identifier.of("rocks", "starfish"))
                    .addOptional(Identifier.of("rocks", "oak_stick"))
                    .addOptionalTag(Identifier.of("somemoreblocks", "leaf_litters"))
                    .addOptionalTag(Identifier.of("somemoreblocks", "mushroom_colonies"));

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
            getOrCreateTagBuilder(NeMuelchTags.EntityTypes.CRATE_STORAGE_BLACKLIST).add(
                    EntityType.ENDER_DRAGON, EntityType.END_CRYSTAL, EntityType.WITHER
            );
            getOrCreateTagBuilder(NeMuelchTags.EntityTypes.OCCASION_DUPLICATION_BLACKLIST)
                    .add(EntityType.ARMOR_STAND, EntityType.ENDER_DRAGON, EntityType.WITHER);
            getOrCreateTagBuilder(NeMuelchTags.EntityTypes.ACID_IMMUNE)
                    .add(EntityType.SLIME);
            getOrCreateTagBuilder(NeMuelchTags.EntityTypes.BUCKLER_SHIELD_KNOCKBACK_IMMUNE)
                    .add(EntityType.ENDER_DRAGON, EntityType.WITHER);
        }
    }

    public static class DamageTypeTagsProvider extends FabricTagProvider<DamageType> {
        public DamageTypeTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(NeMuelchTags.DamageTypes.BLOCKED_BY_SHIELDING_SKIN_EFFECT)
                    .addOptionalTag(DamageTypeTags.IS_PROJECTILE)
                    .addOptionalTag(DamageTypeTags.IS_EXPLOSION)
                    .addOptionalTag(DamageTypeTags.IS_FALL)
                    .addOptionalTag(DamageTypeTags.IS_FIRE)
                    .add(DamageTypes.MAGIC, DamageTypes.FALLING_BLOCK, NeMuelchDamageTypes.ACID_BURN.get());

            Map<TagKey<DamageType>, HashSet<NeMuelchDamageTypes.DamageTypePair>> invertedMap = new HashMap<>();
            for (var entry : NeMuelchDamageTypes.ALL.entrySet()) {
                for (TagKey<DamageType> tag : entry.getValue().tags()) {
                    invertedMap.computeIfAbsent(tag, damageTypeTagKey -> new HashSet<>()).add(entry.getValue());
                }
            }
            for (var entry : invertedMap.entrySet()) {
                FabricTagProvider<DamageType>.FabricTagBuilder builder = getOrCreateTagBuilder(entry.getKey()).setReplace(false);
                for (NeMuelchDamageTypes.DamageTypePair damageTypePair : entry.getValue()) {
                    builder.addOptional(damageTypePair.get());
                }
            }
        }
    }

    public static class BiomeTagProvider extends FabricTagProvider<Biome> {
        public BiomeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.BIOME, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(NeMuelchTags.Biomes.ACIDIC)
                    .add(NeMuelchBiomes.ACIDIC_PLAINS.key());
        }
    }

    public static class DimensionTypeTagProvider extends FabricTagProvider<DimensionType> {
        public DimensionTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.DIMENSION_TYPE, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(NeMuelchTags.DimensionTypes.UNNATURAL)
                    .add(NeMuelchDimensions.BACKYARD.key());
        }
    }

    public static class FluidTagProvider extends FabricTagProvider<Fluid> {
        public FluidTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.FLUID, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            getOrCreateTagBuilder(NeMuelchTags.Fluids.ACID);
        }
    }
}
