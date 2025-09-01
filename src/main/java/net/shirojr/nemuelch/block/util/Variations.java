package net.shirojr.nemuelch.block.util;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Variations {
    public static final List<Variation> ALL_VARIATIONS = new ArrayList<>();

    public static final Variation DIRT = of(new Variation("dirt", Blocks.DIRT, Identifier.tryParse("dirt"), List.of(BlockTags.SHOVEL_MINEABLE, BlockTags.DIRT)));

    public static final Variation STONE = of(new Variation("stone", Blocks.STONE, Identifier.tryParse("stone"), Variation.getStoneTags()));
    public static final Variation STONE_BRICKS = of(new Variation("stone_bricks", Blocks.STONE_BRICKS, Identifier.tryParse("stone_bricks"), Variation.getStoneTags()));
    public static final Variation COBBLESTONE = of(new Variation("cobblestone", Blocks.COBBLESTONE, Identifier.tryParse("cobblestone"), Variation.getStoneTags()));
    public static final Variation BRICKS = of(new Variation("bricks", Blocks.BRICKS, Identifier.tryParse("bricks"), Variation.getStoneTags()));
    public static final Variation SMOOTH_SANDSTONE = of(new Variation("smooth_sandstone", Blocks.SMOOTH_SANDSTONE, Identifier.tryParse("sandstone_top"), Identifier.tryParse("sandstone_top"), Variation.getStoneTags(ConventionalBlockTags.SANDSTONE_BLOCKS)));

    public static final Variation DIORITE = of(new Variation("diorite", Blocks.DIORITE, Identifier.tryParse("diorite"), Variation.getStoneTags()));
    public static final Variation GRANITE = of(new Variation("granite", Blocks.GRANITE, Identifier.tryParse("granite"), Variation.getStoneTags()));
    public static final Variation ANDESITE = of(new Variation("andesite", Blocks.ANDESITE, Identifier.tryParse("andesite"), Variation.getStoneTags()));
    public static final Variation CALCITE = of(new Variation("calcite", Blocks.CALCITE, Identifier.tryParse("calcite"), Variation.getStoneTags()));

    public static final Variation IRON_BLOCK = of(new Variation("iron_block", Blocks.IRON_BLOCK, Identifier.tryParse("iron_block"), List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation GOLD_BLOCK = of(new Variation("gold_block", Blocks.GOLD_BLOCK, Identifier.tryParse("gold_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation DIAMOND_BLOCK = of(new Variation("diamond_block", Blocks.DIAMOND_BLOCK, Identifier.tryParse("diamond_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));
    public static final Variation EMERALD_BLOCK = of(new Variation("emerald_block", Blocks.EMERALD_BLOCK, Identifier.tryParse("emerald_block"), List.of(BlockTags.NEEDS_IRON_TOOL, BlockTags.PICKAXE_MINEABLE)));

    public static final Variation DEEPSLATE_BRICKS = of(new Variation("deepslate_bricks", Blocks.DEEPSLATE_BRICKS, Identifier.tryParse("deepslate_bricks"), Variation.getStoneTags()));
    public static final Variation COBBLED_DEEPSLATE = of(new Variation("cobbled_deepslate", Blocks.COBBLED_DEEPSLATE, Identifier.tryParse("cobbled_deepslate"), Variation.getStoneTags()));
    public static final Variation DEEPSLATE = of(new Variation("deepslate", Blocks.DEEPSLATE, new Identifier("deepslate"), new Identifier("deepslate"), new Identifier("deepslate_top"), Variation.getStoneTags()));

    public static final Variation OAK_LOG = ofLogVariation(Blocks.OAK_LOG, "oak", BlockTags.OAK_LOGS);
    public static final Variation SPRUCE_LOG = ofLogVariation(Blocks.SPRUCE_LOG, "spruce", BlockTags.SPRUCE_LOGS);
    public static final Variation BIRCH_LOG = ofLogVariation(Blocks.BIRCH_LOG, "birch", BlockTags.BIRCH_LOGS);
    public static final Variation ACACIA_LOG = ofLogVariation(Blocks.ACACIA_LOG, "acacia", BlockTags.ACACIA_LOGS);
    public static final Variation DARK_OAK_LOG = ofLogVariation(Blocks.DARK_OAK_LOG, "dark_oak", BlockTags.DARK_OAK_LOGS);
    public static final Variation JUNGLE_LOG = ofLogVariation(Blocks.JUNGLE_LOG, "jungle", BlockTags.JUNGLE_LOGS);
    public static final Variation CHERRY_LOG = ofLogVariation(Blocks.CHERRY_LOG, "cherry", BlockTags.CHERRY_LOGS);
    public static final Variation MANGROVE_LOG = ofLogVariation(Blocks.MANGROVE_LOG, "mangrove", BlockTags.MANGROVE_LOGS);
    public static final Variation CRIMSON_STEM = ofSteVariation(Blocks.CRIMSON_STEM, "crimson", BlockTags.CRIMSON_STEMS);
    public static final Variation WARPED_STEM = ofSteVariation(Blocks.WARPED_STEM, "warped", BlockTags.WARPED_STEMS);


    private static Variation of(Variation variation) {
        ALL_VARIATIONS.add(variation);
        return variation;
    }

    private static Variation ofLogVariation(Block block, String name, TagKey<Block> logTag) {
        return of(
                new Variation(
                        name,
                        block,
                        new Identifier("stripped_%s_log".formatted(name)),
                        new Identifier("%s_log".formatted(name)),
                        new Identifier("%s_log_top".formatted(name)),
                        List.of(logTag)
                )
        );
    }

    private static Variation ofSteVariation(Block block, String name, TagKey<Block> logTag) {
        return of(
                new Variation(
                        name,
                        block,
                        new Identifier("stripped_%s_stem".formatted(name)),
                        new Identifier("%s_stem".formatted(name)),
                        new Identifier("%s_stem_top".formatted(name)),
                        List.of(logTag)
                )
        );
    }
}
