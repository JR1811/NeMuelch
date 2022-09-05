package net.shirojr.nemuelch.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

/*
 * Tags listed in src/main/resources/data/c are listed as common tags
 * and can be used by ModPack creators or other Mods
 *
 * For tags only related to this mod, create them in src/main/resources/nemuelch
 * and call them here with createTag instead of createCommonTag
 */

public class NeMuelchTags {

    public static class Blocks {

        public static final TagKey<Block> HEAT_EMITTING_BLOCKS = createCommonTag("heat_emitting_blocks");

        private static TagKey<Block> createTag(String name) {

            return TagKey.of(Registry.BLOCK_KEY, new Identifier(NeMuelch.MOD_ID, name));
        }

        private static TagKey<Block> createCommonTag(String name) {

            return TagKey.of(Registry.BLOCK_KEY, new Identifier("c", name));
        }
    }

    public static class Items {

        public static final TagKey<Item> NEMUELCH_DRINKS = createCommonTag("nemuelch_drinks");
        public static final TagKey<Item> PESTCANES = createCommonTag("pestcanes");
        public static final TagKey<Item> PESTCANE_UPGRADE_MATERIAL = createCommonTag("pestcane_upgrade_material");

        private static TagKey<Item> createTag(String name) {

            return TagKey.of(Registry.ITEM_KEY ,new Identifier(NeMuelch.MOD_ID, name));
        }

        private static TagKey<Item> createCommonTag(String name) {

            return TagKey.of(Registry.ITEM_KEY ,new Identifier("c", name));
        }
    }
}
