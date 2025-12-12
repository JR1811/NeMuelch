package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.util.Variation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NeMuelchBlockVariations {
    public static final List<Variation> ALL_VARIATIONS = new ArrayList<>();


    public static final Variation DIRT = of(new Variation("dirt", Blocks.DIRT, Identifier.tryParse("dirt"), List.of(BlockTags.SHOVEL_MINEABLE, BlockTags.DIRT)));
    public static final Variation DIRT_PATH = of(new Variation("dirt_path", Blocks.DIRT_PATH, Identifier.tryParse("dirt_path_top"), Identifier.tryParse("dirt_path_top"), List.of(BlockTags.SHOVEL_MINEABLE, BlockTags.DIRT)));
    public static final Variation MUD = of(new Variation("mud", Blocks.MUD, Identifier.tryParse("mud"), List.of(BlockTags.SHOVEL_MINEABLE, BlockTags.DIRT)));
    public static final Variation GRAVEL = of(new Variation("gravel", Blocks.GRAVEL, Identifier.tryParse("gravel"), List.of(BlockTags.SHOVEL_MINEABLE)));

    public static final Variation STONE = of(new Variation("stone", Blocks.STONE, Identifier.tryParse("stone"), Variation.getStoneTags()));
    public static final Variation SMOOTH_STONE = of(new Variation("smooth_stone", Blocks.SMOOTH_STONE, Identifier.tryParse("smooth_stone"), Variation.getStoneTags()));

    public static final Variation STONE_BRICKS = of(new Variation("stone_bricks", Blocks.STONE_BRICKS, Identifier.tryParse("stone_bricks"), Variation.getStoneTags()));
    public static final Variation CRACKED_STONE_BRICKS = of(new Variation("cracked_stone_bricks", Blocks.CRACKED_STONE_BRICKS, Identifier.tryParse("cracked_stone_bricks"), Variation.getStoneTags()));
    public static final Variation MOSSY_STONE_BRICKS = of(new Variation("mossy_stone_bricks", Blocks.CRACKED_STONE_BRICKS, Identifier.tryParse("mossy_stone_bricks"), Variation.getStoneTags()));

    public static final Variation COBBLESTONE = of(new Variation("cobblestone", Blocks.COBBLESTONE, Identifier.tryParse("cobblestone"), Variation.getStoneTags()));
    public static final Variation MOSSY_COBBLESTONE = of(new Variation("mossy_cobblestone", Blocks.MOSSY_COBBLESTONE, Identifier.tryParse("mossy_cobblestone"), Variation.getStoneTags()));

    public static final Variation END_STONE = of(new Variation("end_stone", Blocks.END_STONE, Identifier.tryParse("end_stone"), Variation.getStoneTags()));
    public static final Variation END_STONE_BRICKS = of(new Variation("end_stone_bricks", Blocks.END_STONE_BRICKS, Identifier.tryParse("end_stone_bricks"), Variation.getStoneTags()));

    public static final Variation PURPUR_BLOCK = of(new Variation("purpur_block", Blocks.PURPUR_BLOCK, new Identifier("purpur_block"), Variation.getStoneTags()));

    public static final Variation BRICKS = of(new Variation("bricks", Blocks.BRICKS, Identifier.tryParse("bricks"), Variation.getStoneTags()));
    public static final Variation NETHER_BRICKS = of(new Variation("nether_bricks", Blocks.NETHER_BRICKS, Identifier.tryParse("nether_bricks"), Variation.getStoneTags()));
    public static final Variation MUD_BRICKS = of(new Variation("mud_bricks", Blocks.MUD_BRICKS, Identifier.tryParse("mud_bricks"), Variation.getStoneTags()));

    public static final Variation SAND = of(new Variation("sand", Blocks.SAND, Identifier.tryParse("sand"), List.of(BlockTags.SHOVEL_MINEABLE, BlockTags.SAND)));
    public static final Variation SMOOTH_SANDSTONE = of(new Variation("smooth_sandstone", Blocks.SMOOTH_SANDSTONE, Identifier.tryParse("sandstone_top"), Identifier.tryParse("sandstone_top"), Variation.getStoneTags(ConventionalBlockTags.SANDSTONE_BLOCKS)));
    public static final Variation SMOOTH_RED_SANDSTONE = of(new Variation("smooth_red_sandstone", Blocks.SMOOTH_RED_SANDSTONE, Identifier.tryParse("red_sandstone_top"), Identifier.tryParse("red_sandstone_top"), Variation.getStoneTags(ConventionalBlockTags.RED_SANDSTONE_BLOCKS)));

    public static final Variation DIORITE = of(new Variation("diorite", Blocks.DIORITE, Identifier.tryParse("diorite"), Variation.getStoneTags()));
    public static final Variation GRANITE = of(new Variation("granite", Blocks.GRANITE, Identifier.tryParse("granite"), Variation.getStoneTags()));
    public static final Variation ANDESITE = of(new Variation("andesite", Blocks.ANDESITE, Identifier.tryParse("andesite"), Variation.getStoneTags()));
    public static final Variation CALCITE = of(new Variation("calcite", Blocks.CALCITE, Identifier.tryParse("calcite"), Variation.getStoneTags()));
    public static final Variation TUFF = of(new Variation("tuff", Blocks.TUFF, Identifier.tryParse("tuff"), Variation.getStoneTags()));

    public static final Variation OCHRE_FROGLIGHT = of(new Variation("ochre_froglight", Blocks.OCHRE_FROGLIGHT, new Identifier("ochre_froglight_side"), new Identifier("ochre_froglight_side"), new Identifier("ochre_froglight_top"), new Identifier("ochre_froglight_top"), List.of()));
    public static final Variation VERDANT_FROGLIGHT = of(new Variation("verdant_froglight", Blocks.VERDANT_FROGLIGHT, new Identifier("verdant_froglight_side"), new Identifier("verdant_froglight_side"), new Identifier("verdant_froglight_top"), new Identifier("verdant_froglight_top"), List.of()));
    public static final Variation PEARLESCENT_FROGLIGHT = of(new Variation("pearlescent_froglight", Blocks.PEARLESCENT_FROGLIGHT, new Identifier("pearlescent_froglight_side"), new Identifier("pearlescent_froglight_side"), new Identifier("pearlescent_froglight_top"), new Identifier("pearlescent_froglight_top"), List.of()));

    public static final Variation COAL_BLOCK = of(new Variation("coal_block", Blocks.COAL_BLOCK, Identifier.tryParse("coal_block"), List.of(BlockTags.PICKAXE_MINEABLE)));
    public static final Variation IRON_BLOCK = of(new Variation("iron_block", Blocks.IRON_BLOCK, Identifier.tryParse("iron_block"), List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation GOLD_BLOCK = of(new Variation("gold_block", Blocks.GOLD_BLOCK, Identifier.tryParse("gold_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation DIAMOND_BLOCK = of(new Variation("diamond_block", Blocks.DIAMOND_BLOCK, Identifier.tryParse("diamond_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation EMERALD_BLOCK = of(new Variation("emerald_block", Blocks.EMERALD_BLOCK, Identifier.tryParse("emerald_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation NETHERITE_BLOCK = of(new Variation("netherite_block", Blocks.NETHERITE_BLOCK, Identifier.tryParse("netherite_block"), List.of(BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation QUARTZ_BLOCK = of(new Variation("quartz_block", Blocks.QUARTZ_BLOCK, new Identifier("quartz_block_side"), new Identifier("quartz_block_side"), new Identifier("quartz_block_top"), new Identifier("quartz_block_top"), Variation.getStoneTags()));
    public static final Variation AMETHYST_BLOCK = of(new Variation("amethyst_block", Blocks.AMETHYST_BLOCK, new Identifier("amethyst_block"), Variation.getStoneTags()));
    public static final Variation LAPIS_BLOCK = of(new Variation("lapis_block", Blocks.LAPIS_BLOCK, new Identifier("lapis_block"), Variation.getStoneTags()));
    public static final Variation REDSTONE_BLOCK = of(new Variation("redstone_block", Blocks.REDSTONE_BLOCK, new Identifier("redstone_block"), Variation.getStoneTags()));
    public static final Variation DRIPSTONE_BLOCK = of(new Variation("dripstone_block", Blocks.DRIPSTONE_BLOCK, new Identifier("dripstone_block"), Variation.getStoneTags()));
    public static final Variation BONE_BLOCK = of(new Variation("bone_block", Blocks.BONE_BLOCK, new Identifier("bone_block_side"), new Identifier("bone_block_side"), new Identifier("bone_block_top"), new Identifier("bone_block_side"), Variation.getStoneTags()));
    public static final Variation DRIED_KELP_BLOCK = of(new Variation("dried_kelp_block", Blocks.DRIED_KELP_BLOCK, new Identifier("dried_kelp_side"), new Identifier("dried_kelp_side"), new Identifier("dried_kelp_top"), new Identifier("dried_kelp_side"), List.of()));
    public static final Variation HAY_BLOCK = of(new Variation("hay_block", Blocks.HAY_BLOCK, new Identifier("hay_block_side"), new Identifier("hay_block_side"), new Identifier("hay_block_top"), new Identifier("hay_block_side"), List.of()));
    public static final Variation MOSS_BLOCK = of(new Variation("moss_block", Blocks.MOSS_BLOCK, Identifier.tryParse("moss_block"), List.of(BlockTags.DIRT, BlockTags.HOE_MINEABLE)));

    public static final Variation COAL_ORE = of(new Variation("coal_ore", Blocks.COAL_ORE, Identifier.tryParse("coal_ore"), Identifier.tryParse("stone"), List.of(BlockTags.COAL_ORES, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation DEEPSLATE_COAL_ORE = of(new Variation("deepslate_coal_ore", Blocks.DEEPSLATE_COAL_ORE, Identifier.tryParse("deepslate_coal_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.COAL_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL)));
    public static final Variation COPPER_ORE = of(new Variation("copper_ore", Blocks.COPPER_ORE, Identifier.tryParse("copper_ore"), Identifier.tryParse("stone"), List.of(BlockTags.COPPER_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL)));
    public static final Variation DEEPSLATE_COPPER_ORE = of(new Variation("deepslate_copper_ore", Blocks.DEEPSLATE_COPPER_ORE, Identifier.tryParse("deepslate_copper_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.COPPER_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL)));
    public static final Variation IRON_ORE = of(new Variation("iron_ore", Blocks.IRON_ORE, Identifier.tryParse("iron_ore"), Identifier.tryParse("stone"), List.of(BlockTags.IRON_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL)));
    public static final Variation DEEPSLATE_IRON_ORE = of(new Variation("deepslate_iron_ore", Blocks.DEEPSLATE_IRON_ORE, Identifier.tryParse("deepslate_iron_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.IRON_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL)));
    public static final Variation GOLD_ORE = of(new Variation("gold_ore", Blocks.GOLD_ORE, Identifier.tryParse("gold_ore"), Identifier.tryParse("stone"), List.of(BlockTags.GOLD_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation DEEPSLATE_GOLD_ORE = of(new Variation("deepslate_gold_ore", Blocks.DEEPSLATE_GOLD_ORE, Identifier.tryParse("deepslate_gold_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.GOLD_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation DIAMOND_ORE = of(new Variation("diamond_ore", Blocks.DIAMOND_ORE, Identifier.tryParse("diamond_ore"), Identifier.tryParse("stone"), List.of(BlockTags.DIAMOND_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation DEEPSLATE_DIAMOND_ORE = of(new Variation("deepslate_diamond_ore", Blocks.DEEPSLATE_DIAMOND_ORE, Identifier.tryParse("deepslate_diamond_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.DIAMOND_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation EMERALD_ORE = of(new Variation("emerald_ore", Blocks.EMERALD_ORE, Identifier.tryParse("emerald_ore"), Identifier.tryParse("stone"), List.of(BlockTags.EMERALD_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation DEEPSLATE_EMERALD_ORE = of(new Variation("deepslate_emerald_ore", Blocks.DEEPSLATE_EMERALD_ORE, Identifier.tryParse("deepslate_emerald_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.EMERALD_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation REDSTONE_ORE = of(new Variation("redstone_ore", Blocks.REDSTONE_ORE, Identifier.tryParse("redstone_ore"), Identifier.tryParse("stone"), List.of(BlockTags.REDSTONE_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));
    public static final Variation DEEPSLATE_REDSTONE_ORE = of(new Variation("deepslate_redstone_ore", Blocks.DEEPSLATE_REDSTONE_ORE, Identifier.tryParse("deepslate_redstone_ore"), Identifier.tryParse("deepslate"), List.of(BlockTags.REDSTONE_ORES, BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_IRON_TOOL)));

    public static final Variation IRON_BARS = of(new Variation("iron_bars", Blocks.IRON_BARS, Identifier.tryParse("iron_bars"), Variation.getStoneTags()));

    public static final Variation DEEPSLATE_BRICKS = of(new Variation("deepslate_bricks", Blocks.DEEPSLATE_BRICKS, Identifier.tryParse("deepslate_bricks"), Variation.getStoneTags()));
    public static final Variation COBBLED_DEEPSLATE = of(new Variation("cobbled_deepslate", Blocks.COBBLED_DEEPSLATE, Identifier.tryParse("cobbled_deepslate"), Variation.getStoneTags()));
    public static final Variation DEEPSLATE = of(new Variation("deepslate", Blocks.DEEPSLATE, new Identifier("deepslate"), new Identifier("deepslate"), new Identifier("deepslate_top"), Variation.getStoneTags()));

    public static final Variation BASALT = of(new Variation("basalt", Blocks.BASALT, new Identifier("basalt_side"), new Identifier("basalt_side"), new Identifier("basalt_top"), new Identifier("basalt_top"), Variation.getStoneTags()));
    public static final Variation SMOOTH_BASALT = of(new Variation("smooth_basalt", Blocks.SMOOTH_BASALT, new Identifier("smooth_basalt"), Variation.getStoneTags()));

    public static final Variation BLACKSTONE = of(new Variation("blackstone", Blocks.BLACKSTONE, new Identifier("blackstone"), Variation.getStoneTags()));
    public static final Variation GILDED_BLACKSTONE = of(new Variation("gilded_blackstone", Blocks.GILDED_BLACKSTONE, new Identifier("gilded_blackstone"), Variation.getStoneTags()));

    public static final Variation NETHERRACK = of(new Variation("netherrack", Blocks.NETHERRACK, new Identifier("netherrack"), Variation.getStoneTags()));

    public static final Variation PRISMARINE = of(new Variation("prismarine", Blocks.PRISMARINE, Identifier.tryParse("prismarine"), Variation.getStoneTags()));
    public static final Variation PRISMARINE_BRICKS = of(new Variation("prismarine_bricks", Blocks.PRISMARINE_BRICKS, Identifier.tryParse("prismarine_bricks"), Variation.getStoneTags()));

    public static final Variation WAXED_COPPER_BLOCK = of(new Variation("waxed_copper_block", Blocks.WAXED_COPPER_BLOCK, Identifier.tryParse("copper_block"), Identifier.tryParse("cut_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_CUT_COPPER = of(new Variation("waxed_cut_copper", Blocks.WAXED_CUT_COPPER, Identifier.tryParse("cut_copper"), Identifier.tryParse("cut_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_EXPOSED_COPPER = of(new Variation("waxed_exposed_copper", Blocks.WAXED_EXPOSED_COPPER, Identifier.tryParse("exposed_copper"), Identifier.tryParse("exposed_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_EXPOSED_CUT_COPPER = of(new Variation("waxed_exposed_cut_copper", Blocks.WAXED_EXPOSED_CUT_COPPER, Identifier.tryParse("exposed_cut_copper"), Identifier.tryParse("exposed_cut_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_WEATHERED_COPPER = of(new Variation("waxed_weathered_copper", Blocks.WAXED_WEATHERED_COPPER, Identifier.tryParse("weathered_copper"), Identifier.tryParse("weathered_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_WEATHERED_CUT_COPPER = of(new Variation("waxed_weathered_cut_copper", Blocks.WAXED_WEATHERED_CUT_COPPER, Identifier.tryParse("weathered_cut_copper"), Identifier.tryParse("weathered_cut_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_OXIDIZED_COPPER = of(new Variation("waxed_oxidized_copper", Blocks.WAXED_OXIDIZED_COPPER, Identifier.tryParse("oxidized_copper"), Identifier.tryParse("oxidized_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation WAXED_OXIDIZED_CUT_COPPER = of(new Variation("waxed_oxidized_cut_copper", Blocks.WAXED_OXIDIZED_CUT_COPPER, Identifier.tryParse("oxidized_cut_copper"), Identifier.tryParse("oxidized_cut_copper"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));

    public static final Variation BARREL_BOTTOM = of(new Variation("barrel_bottom", Blocks.BARREL, Identifier.tryParse("barrel_bottom"), Identifier.tryParse("spruce_planks"), List.of(BlockTags.AXE_MINEABLE)));
    public static final Variation BARREL_LID = of(new Variation("barrel_lid", Blocks.BARREL, Identifier.tryParse("barrel_top"), Identifier.tryParse("spruce_planks"), List.of(BlockTags.AXE_MINEABLE)));
    public static final Variation BARREL_SIDE = of(new Variation("barrel_side", Blocks.BARREL, Identifier.tryParse("barrel_side"), Identifier.tryParse("spruce_planks"), List.of(BlockTags.AXE_MINEABLE)));

    public static final List<Variation> OAK_LOG = ofWoodVariations(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG, "oak_log", BlockTags.OAK_LOGS);
    public static final List<Variation> SPRUCE_LOG = ofWoodVariations(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG, "spruce_log", BlockTags.SPRUCE_LOGS);
    public static final List<Variation> BIRCH_LOG = ofWoodVariations(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG, "birch_log", BlockTags.BIRCH_LOGS);
    public static final List<Variation> ACACIA_LOG = ofWoodVariations(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG, "acacia_log", BlockTags.ACACIA_LOGS);
    public static final List<Variation> DARK_OAK_LOG = ofWoodVariations(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG, "dark_oak_log", BlockTags.DARK_OAK_LOGS);
    public static final List<Variation> JUNGLE_LOG = ofWoodVariations(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG, "jungle_log", BlockTags.JUNGLE_LOGS);
    public static final List<Variation> CHERRY_LOG = ofWoodVariations(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG, "cherry_log", BlockTags.CHERRY_LOGS);
    public static final List<Variation> MANGROVE_LOG = ofWoodVariations(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG, "mangrove_log", BlockTags.MANGROVE_LOGS);
    public static final List<Variation> CRIMSON_STEM = ofWoodVariations(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, "crimson_stem", BlockTags.CRIMSON_STEMS);
    public static final List<Variation> WARPED_STEM = ofWoodVariations(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, "warped_stem", BlockTags.WARPED_STEMS);
    public static final List<Variation> BAMBOO_BLOCK = ofWoodVariations(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK, "bamboo_block", BlockTags.BAMBOO_BLOCKS);

    static {
        for (Block registryBlock : Registries.BLOCK) {
            Identifier registryBlockId = Registries.BLOCK.getId(registryBlock);
            if (!registryBlockId.getNamespace().equals("minecraft")) continue;
            String blockName = registryBlockId.getPath();

            if (blockName.endsWith("terracotta")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), Variation.getStoneTags(BlockTags.TERRACOTTA)));
            } else if (blockName.endsWith("wool")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), List.of(BlockTags.WOOL)));
            } else if (blockName.endsWith("concrete")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), Variation.getStoneTags()));
            } else if (blockName.endsWith("planks")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), List.of(BlockTags.PLANKS)));
            } else if (blockName.endsWith("stained_glass")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), List.of(ConventionalBlockTags.GLASS_BLOCKS)));
            } else if (blockName.endsWith("trapdoor")) {
                ALL_VARIATIONS.add(new Variation(blockName, registryBlock, Identifier.tryParse(blockName), List.of(BlockTags.TRAPDOORS)));
            }
        }
    }

    private static Variation of(Variation variation) {
        ALL_VARIATIONS.add(variation);
        return variation;
    }

    private static List<Variation> ofWoodVariations(Block base, Block stripped, String name, TagKey<Block> logTag) {
        Variation logVariation = of(
                new Variation(
                        name,
                        base,
                        new Identifier("stripped_%s".formatted(name)),
                        new Identifier("%s".formatted(name)),
                        new Identifier("%s_top".formatted(name)),
                        List.of(logTag)
                )
        );
        Variation strippedLogVariation = of(
                new Variation(
                        "stripped_" + name,
                        stripped,
                        new Identifier("stripped_%s".formatted(name)),
                        new Identifier("stripped_%s".formatted(name)),
                        new Identifier("stripped_%s_top".formatted(name)),
                        List.of(logTag)
                )
        );
        return List.of(logVariation, strippedLogVariation);
    }

    @Nullable
    public static Variation fromParent(Block block) {
        for (Variation entry : ALL_VARIATIONS) {
            if (entry.parentBlock().equals(block)) return entry;
        }
        return null;
    }
}
