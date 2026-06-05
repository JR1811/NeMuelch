package net.shirojr.nemuelch.init;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
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
        Biomes.initialize();
        DimensionTypes.initialize();
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
        public static final TagKey<Block> CRATES = createTag("crates");
        public static final TagKey<Block> LANTERNS = createCommonTag("lanterns");
        public static final TagKey<Block> CHAINED_MACE_BLACKLIST = createTag("chained_mace_blacklist");

        public static final TagKey<Block> CHAINED_MACE_DEATH = createTag("chained_mace_death");
        public static final TagKey<Block> CHAINED_MACE_BURN = createTag("chained_mace_burn");
        public static final TagKey<Block> CHAINED_MACE_HUNGER = createTag("chained_mace_hunger");
        public static final TagKey<Block> CHAINED_MACE_POISON = createTag("chained_mace_poison");
        public static final TagKey<Block> CHAINED_MACE_WITHER = createTag("chained_mace_wither");
        public static final TagKey<Block> CHAINED_MACE_SLIME = createTag("chained_mace_slime");
        public static final List<TagKey<Block>> CHAINED_MACE_EFFECT_BLOCKS = List.of(
                CHAINED_MACE_BURN, CHAINED_MACE_HUNGER, CHAINED_MACE_POISON, CHAINED_MACE_WITHER, CHAINED_MACE_SLIME
        );
        public static final TagKey<Block> TERRAFORM_PREPARATION = createTag("terraform_preparation");


        private static TagKey<Block> createTag(String name) {
            TagKey<Block> tagKey = TagKey.of(RegistryKeys.BLOCK, NeMuelch.getId(name));
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
        public static final TagKey<Item> BLOCK_FIRST_PERSON_RENDERING = createTag("block_first_person_rendering");
        public static final TagKey<Item> CRATES = createTag("crates");
        public static final TagKey<Item> CRATE_STANDS = createTag("crate_stands");
        public static final TagKey<Item> STRIPPED_LOGS = createCommonTag("stripped_logs");
        public static final TagKey<Item> SOAP = createCommonTag("soap");
        public static final TagKey<Item> DUMMY_CLEAR = createTag("dummy_clear");
        public static final TagKey<Item> DUMMY_UNDEAD = createTag("dummy_undead");
        public static final TagKey<Item> DUMMY_ARTHROPOD = createTag("dummy_arthropod");
        public static final TagKey<Item> DUMMY_ILLAGER = createTag("dummy_illager");
        public static final TagKey<Item> DUMMY_AQUATIC = createTag("dummy_aquatic");

        private static TagKey<Item> createTag(String name) {
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, NeMuelch.getId(name));
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
        public static final TagKey<EntityType<?>> DESPAWN_PROTECTED = createTag("despawn_protected");
        public static final TagKey<EntityType<?>> CRATE_STORAGE_BLACKLIST = createTag("crate_storage_blacklist");
        public static final TagKey<EntityType<?>> OCCASION_DUPLICATION_BLACKLIST = createTag("occasion_duplication_blacklist");

        private static TagKey<EntityType<?>> createTag(String name) {
            TagKey<EntityType<?>> tagKey = TagKey.of(RegistryKeys.ENTITY_TYPE, NeMuelch.getId(name));
            ALL_ENTITY_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class Biomes {
        public static final List<TagKey<Biome>> ALL_BIOME_TAGS = new ArrayList<>();

        public static final TagKey<Biome> ACIDIC = createTag("acidic");

        private static TagKey<Biome> createTag(String name) {
            TagKey<Biome> tagKey = TagKey.of(RegistryKeys.BIOME, NeMuelch.getId(name));
            ALL_BIOME_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }

    public static class DimensionTypes {
        public static final List<TagKey<DimensionType>> ALL_DIMENSION_TYPE_TAGS = new ArrayList<>();

        public static final TagKey<DimensionType> UNNATURAL = createTag("unnatural");

        private static TagKey<DimensionType> createTag(String name) {
            TagKey<DimensionType> tagKey = TagKey.of(RegistryKeys.DIMENSION_TYPE, NeMuelch.getId(name));
            ALL_DIMENSION_TYPE_TAGS.add(tagKey);
            ALL_TAGS.add(tagKey);
            return tagKey;
        }

        public static void initialize() {
            // static initialisation
        }
    }
}
