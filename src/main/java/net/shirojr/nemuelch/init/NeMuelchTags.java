package net.shirojr.nemuelch.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

@SuppressWarnings({"unused", "SameParameterValue"})
public class NeMuelchTags {
    public static class Blocks {
        public static final TagKey<Block> HEAT_EMITTING_BLOCKS = createCommonTag("heat_emitting_blocks");
        public static final TagKey<Block> TORCH_IGNITING_BLOCKS = createCommonTag("torch_igniting_blocks");
        public static final TagKey<Block> KNOCK_SOUND_BLOCKS = createTag("knock_sound_blocks");

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
        public static final TagKey<Item> ARKADUSCANE_PROJECTILE = createCommonTag("arkaduscane_projectile");
        public static final TagKey<Item> CAMPFIRE_IGNITER = createCommonTag("campfire_igniter");
        public static final TagKey<Item> SHIELD_REPAIR_MATERIAL = createCommonTag("nemuelch_shield_repair");
        public static final TagKey<Item> ROPER_ROPES = createCommonTag("ropes");
        public static final TagKey<Item> GLOVES = createCommonTag("gloves");
        public static final TagKey<Item> PULL_BODY_TOOLS = createCommonTag("pull_body_tools");
        public static final TagKey<Item> IGNITES_POTS = createCommonTag("ignites_tnt_in_drop_pots");

        private static TagKey<Item> createTag(String name) {
            return TagKey.of(Registry.ITEM_KEY, new Identifier(NeMuelch.MOD_ID, name));
        }

        private static TagKey<Item> createCommonTag(String name) {
            return TagKey.of(Registry.ITEM_KEY, new Identifier("c", name));
        }
    }
}
