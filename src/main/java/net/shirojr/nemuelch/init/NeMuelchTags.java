package net.shirojr.nemuelch.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SameParameterValue"})
public class NeMuelchTags {
    public static final List<TagKey<?>> ALL_TAGS = new ArrayList<>();

    public static class Blocks {
        public static final List<TagKey<Block>> ALL_BLOCK_TAGS = new ArrayList<>();

        public static final TagKey<Block> HEAT_EMITTING_BLOCKS = createTag("heat_emitting_blocks");
        public static final TagKey<Block> TORCH_IGNITING_BLOCKS = createTag("torch_igniting_blocks");
        public static final TagKey<Block> KNOCK_SOUND_BLOCKS = createTag("knock_sound_blocks");

        private static TagKey<Block> createTag(String name) {
            TagKey<Block> tagKey = TagKey.of(Registry.BLOCK_KEY, new Identifier(NeMuelch.MOD_ID, name));
            ALL_BLOCK_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        private static TagKey<Block> createCommonTag(String name) {
            TagKey<Block> tagKey = TagKey.of(Registry.BLOCK_KEY, new Identifier("c", name));
            ALL_BLOCK_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }
    }

    public static class Items {
        public static final List<TagKey<Item>> ALL_ITEM_TAGS = new ArrayList<>();

        public static final TagKey<Item> NEMUELCH_DRINKS = createTag("nemuelch_drinks");
        public static final TagKey<Item> PESTCANES = createTag("pestcanes");
        public static final TagKey<Item> PESTCANE_UPGRADE_MATERIAL = createTag("pestcane_upgrade_material");
        public static final TagKey<Item> ARKADUSCANE_PROJECTILE = createTag("arkaduscane_projectile");
        public static final TagKey<Item> CAMPFIRE_IGNITER = createTag("campfire_igniter");
        public static final TagKey<Item> SHIELD_REPAIR_MATERIAL = createTag("nemuelch_shield_repair");
        public static final TagKey<Item> ROPER_ROPES = createTag("ropes");
        public static final TagKey<Item> GLOVES = createCommonTag("gloves");
        public static final TagKey<Item> PULL_BODY_TOOLS = createTag("pull_body_tools");
        public static final TagKey<Item> IGNITES_POTS = createTag("ignites_tnt_in_drop_pots");

        private static TagKey<Item> createTag(String name) {
            TagKey<Item> tagKey = TagKey.of(Registry.ITEM_KEY, new Identifier(NeMuelch.MOD_ID, name));
            ALL_ITEM_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        private static TagKey<Item> createCommonTag(String name) {
            TagKey<Item> tagKey = TagKey.of(Registry.ITEM_KEY, new Identifier("c", name));
            ALL_ITEM_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }
    }
}
