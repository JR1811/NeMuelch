package net.shirojr.nemuelch.init;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SameParameterValue"})
public class NeMuelchTags {
    public static final List<TagKey<?>> ALL_TAGS = new ArrayList<>();

    public static void initialize() {
        // static initialisation
        Blocks.initialize();
        Items.initialize();
        EntityTypes.initialize();
    }

    public static class Blocks {
        public static final List<TagKey<Block>> ALL_BLOCK_TAGS = new ArrayList<>();

        public static final TagKey<Block> HEAT_EMITTING_BLOCKS = createTag("heat_emitting_blocks");
        public static final TagKey<Block> TORCH_IGNITING_BLOCKS = createTag("torch_igniting_blocks");
        public static final TagKey<Block> KNOCK_SOUND_BLOCKS = createTag("knock_sound_blocks");
        public static final TagKey<Block> LIFT_ROPE_ANCHOR = createTag("lift_rope_anchor");
        public static final TagKey<Block> FERTILIZABLE_WHITELIST = createTag("fertilizable_whitelist");
        public static final TagKey<Block> NEVER_BLIGHT = createTag("never_blight_blocks");
        public static final TagKey<Block> SIGIL_COLOR_BLOCKS = createTag("sigil_color_blocks");
        public static final TagKey<Block> IGNORED_BY_SHOVEL_FLATTENING = createTag("ignored_by_shovel_flattening");
        public static final TagKey<Block> DEEP_WATER_INCLUSIVE = createTag("deep_water_inclusive");


        private static TagKey<Block> createTag(String name) {
            TagKey<Block> tagKey = TagKey.of(RegistryKeys.BLOCK, new Identifier(NeMuelch.MOD_ID, name));
            ALL_BLOCK_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        private static TagKey<Block> createCommonTag(String name) {
            TagKey<Block> tagKey = TagKey.of(RegistryKeys.BLOCK, new Identifier("c", name));
            ALL_BLOCK_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
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
        public static final TagKey<Item> ANTI_VAMPIRE = createTag("anti_vampire");
        public static final TagKey<Item> BOOK_WRAPPER_CONTENT = createTag("book_wrapper_content");
        public static final TagKey<Item> NO_FOOD_STACK_DECREMENT = createTag("no_food_stack_decrement");
        public static final TagKey<Item> NEVER_BLIGHT = createTag("never_blight_items");
        public static final TagKey<Item> SOAP_COATABLE = createTag("soap_coatable");
        public static final TagKey<Item> BLOCK_THIRD_PERSON_RENDERING = createTag("block_third_person_rendering");

        private static TagKey<Item> createTag(String name) {
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, new Identifier(NeMuelch.MOD_ID, name));
            ALL_ITEM_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        private static TagKey<Item> createCommonTag(String name) {
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, new Identifier("c", name));
            ALL_ITEM_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class EntityTypes {
        public static final List<TagKey<EntityType<?>>> ALL_ENTITY_TAGS = new ArrayList<>();

        public static final TagKey<EntityType<?>> VAMPIRE_INDIGESTIBLE = createTag("vampire_indigestible");
        public static final TagKey<EntityType<?>> UNSINKABLE = createTag("unsinkable");

        private static TagKey<EntityType<?>> createTag(String name) {
            TagKey<EntityType<?>> tagKey = TagKey.of(RegistryKeys.ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name));
            ALL_ENTITY_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }
}
