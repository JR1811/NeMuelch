package net.shirojr.nemuelch.util;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
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

        public static final Tag.Identified<Block> HEAT_EMITTING_BLOCKS = createCommonTag("heat_emitting_blocks");

        private static Tag.Identified<Block> cteateTag(String name) {

            return TagFactory.BLOCK.create(new Identifier(NeMuelch.MOD_ID, name));
        }

        private static Tag.Identified<Block> createCommonTag(String name) {

            return TagFactory.BLOCK.create(new Identifier("c", name));
        }
    }

    public static class Items {

        public static final Tag.Identified<Item> NEMUELCH_DRINKS = createCommonTag("nemuelch_drinks");
        public static final Tag.Identified<Item> PESTCANES = createCommonTag("pestcanes");

        private static Tag.Identified<Item> cteateTag(String name) {

            return TagFactory.ITEM.create(new Identifier(NeMuelch.MOD_ID, name));
        }

        private static Tag.Identified<Item> createCommonTag(String name) {

            return TagFactory.ITEM.create(new Identifier("c", name));
        }
    }
}
