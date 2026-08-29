package net.shirojr.nemuelch.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.ItemTags;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.compat.mythicmetals.MythicMetalsItemsCompat;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;

import java.util.function.Consumer;

public class NeMuelchRecipeGenerator extends FabricRecipeProvider {
    public NeMuelchRecipeGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> consumer) {
        generateMuelchDrinks(consumer);
        generateToolsWeaponsShields(consumer);
        generateSupportItems(consumer);
        generateBlocks(consumer);
        generateMeatLumps(consumer);
        generateCrates(consumer);
        generateTraps(consumer);
        generateMisc(consumer);
        generateClimbingPickaxes(consumer);
    }

    private static void generateMuelchDrinks(Consumer<RecipeJsonProvider> consumer) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, NeMuelchItems.GREEN_MUELCH)
                .input(Items.MILK_BUCKET)
                .input(Items.CACTUS)
                .input(Items.SUGAR, 2)
                .input(Items.GLASS_BOTTLE, 3)
                .criterion(FabricRecipeProvider.hasItem(Items.MILK_BUCKET), FabricRecipeProvider.conditionsFromItem(Items.MILK_BUCKET))
                .criterion(FabricRecipeProvider.hasItem(Items.GLASS_BOTTLE), FabricRecipeProvider.conditionsFromItem(Items.GLASS_BOTTLE))
                .criterion(FabricRecipeProvider.hasItem(Items.CACTUS), FabricRecipeProvider.conditionsFromItem(Items.CACTUS))
                .criterion(FabricRecipeProvider.hasItem(Items.SUGAR), FabricRecipeProvider.conditionsFromItem(Items.SUGAR))
                .offerTo(consumer);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, NeMuelchItems.BLUE_MUELCH)
                .input(NeMuelchItems.GREEN_MUELCH)
                .input(Items.SEA_PICKLE)
                .input(Items.BLUE_DYE)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.GREEN_MUELCH), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.GREEN_MUELCH))
                .offerTo(consumer);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, NeMuelchItems.BROWN_MUELCH)
                .input(NeMuelchItems.GREEN_MUELCH)
                .input(Items.SUGAR)
                .input(Items.COARSE_DIRT)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.GREEN_MUELCH), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.GREEN_MUELCH))
                .offerTo(consumer);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, NeMuelchItems.PINK_MUELCH)
                .input(NeMuelchItems.GREEN_MUELCH)
                .input(Items.SWEET_BERRIES)
                .input(Items.SUGAR)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.GREEN_MUELCH), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.GREEN_MUELCH))
                .offerTo(consumer);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.FOOD, NeMuelchItems.YELLOW_MUELCH)
                .input(NeMuelchItems.GREEN_MUELCH)
                .input(Items.PUFFERFISH)
                .input(Items.DEAD_BUSH)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.GREEN_MUELCH), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.GREEN_MUELCH))
                .offerTo(consumer);
    }

    private static void generateToolsWeaponsShields(Consumer<RecipeJsonProvider> consumer) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.FORTIFIED_SHIELD)
                .pattern("ili")
                .pattern("isi")
                .pattern("iti")
                .input('s', Items.SHIELD)
                .input('l', Items.LAPIS_LAZULI)
                .input('i', Items.IRON_INGOT)
                .input('t', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(Items.SHIELD), FabricRecipeProvider.conditionsFromItem(Items.SHIELD))
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT), FabricRecipeProvider.conditionsFromItem(Items.IRON_INGOT))
                .criterion(FabricRecipeProvider.hasItem(Items.LAPIS_LAZULI), FabricRecipeProvider.conditionsFromItem(Items.LAPIS_LAZULI))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.PEST_CANE)
                .pattern("fss")
                .pattern("f  ")
                .pattern("f  ")
                .input('f', ItemTags.WOODEN_FENCES)
                .input('s', Items.STICK)
                .criterion("has_wooden_fences", FabricRecipeProvider.conditionsFromTag(ItemTags.WOODEN_FENCES))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.GLOVE)
                .pattern("LLL")
                .pattern("LWL")
                .pattern("LSL")
                .input('L', Items.LEATHER)
                .input('W', ItemTags.WOOL)
                .input('S', Items.STRING)
                .criterion("has_wool", FabricRecipeProvider.conditionsFromTag(ItemTags.WOOL))
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, NeMuelchItems.SHIELD_BUCKLER)
                .pattern(" p ")
                .pattern("pip")
                .pattern(" p ")
                .input('p', ItemTags.PLANKS)
                .input('i', Items.IRON_INGOT)
                .criterion("has_planks", conditionsFromTag(ItemTags.PLANKS))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(consumer);
    }

    private static void generateSupportItems(Consumer<RecipeJsonProvider> consumer) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.DROP_POT_BLOCK)
                .pattern(" b ")
                .pattern("b b")
                .pattern("bbb")
                .input('b', Items.BRICK)
                .criterion(FabricRecipeProvider.hasItem(Items.BRICK), FabricRecipeProvider.conditionsFromItem(Items.BRICK))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.BANDAGE)
                .pattern("PPP")
                .pattern("SOS")
                .pattern("   ")
                .input('P', Items.PAPER)
                .input('O', NeMuelchItems.OINTMENT)
                .input('S', Items.STRING)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.OINTMENT), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.OINTMENT))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.BANDAGE), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.BANDAGE))
                .offerTo(consumer);

        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.OINTMENT)
                .input(Items.HONEY_BOTTLE)
                .input(Items.BOWL)
                .input(Items.GRASS)
                .input(Items.POPPY)
                .criterion(FabricRecipeProvider.hasItem(Items.HONEY_BOTTLE), FabricRecipeProvider.conditionsFromItem(Items.HONEY_BOTTLE))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.OMINOUS_HEART)
                .pattern(" B ")
                .pattern("BEB")
                .pattern(" B ")
                .input('B', Items.BEEF)
                .input('E', Items.ENDER_EYE)
                .criterion(FabricRecipeProvider.hasItem(Items.ENDER_EYE), FabricRecipeProvider.conditionsFromItem(Items.ENDER_EYE))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.PORTABLE_BARREL)
                .pattern(" WL")
                .pattern(" BL")
                .pattern("   ")
                .input('W', Items.BUCKET)
                .input('L', Items.LEAD)
                .input('B', Blocks.BARREL)
                .criterion(FabricRecipeProvider.hasItem(Items.BUCKET), FabricRecipeProvider.conditionsFromItem(Items.BUCKET))
                .criterion(FabricRecipeProvider.hasItem(Items.LEAD), FabricRecipeProvider.conditionsFromItem(Items.LEAD))
                .criterion(FabricRecipeProvider.hasItem(Blocks.BARREL), FabricRecipeProvider.conditionsFromItem(Blocks.BARREL))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchBlocks.ROPE, 8)
                .pattern(" S ")
                .pattern("WWW")
                .pattern(" S ")
                .input('S', Items.STRING)
                .input('W', Items.WHEAT)
                .criterion(FabricRecipeProvider.hasItem(Items.WHEAT), FabricRecipeProvider.conditionsFromItem(Items.WHEAT))
                .criterion(FabricRecipeProvider.hasItem(Items.STRING), FabricRecipeProvider.conditionsFromItem(Items.STRING))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.WATERING_CAN_COPPER)
                .pattern("   ")
                .pattern("lbi")
                .pattern(" ci")
                .input('l', Blocks.LIGHTNING_ROD)
                .input('b', Items.WATER_BUCKET)
                .input('i', Items.COPPER_INGOT)
                .input('c', Blocks.COPPER_BLOCK)
                .criterion(FabricRecipeProvider.hasItem(Blocks.LIGHTNING_ROD), FabricRecipeProvider.conditionsFromItem(Blocks.LIGHTNING_ROD))
                .criterion(FabricRecipeProvider.hasItem(Blocks.COPPER_BLOCK), FabricRecipeProvider.conditionsFromItem(Blocks.COPPER_BLOCK))
                .criterion(FabricRecipeProvider.hasItem(Items.WATER_BUCKET), FabricRecipeProvider.conditionsFromItem(Items.WATER_BUCKET))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.WATERING_CAN_IRON)
                .pattern(" i ")
                .pattern("iwi")
                .pattern(" i ")
                .input('i', Items.IRON_INGOT)
                .input('w', NeMuelchItems.WATERING_CAN_COPPER)
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT), FabricRecipeProvider.conditionsFromItem(Items.IRON_INGOT))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.WATERING_CAN_COPPER), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.WATERING_CAN_COPPER))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.WATERING_CAN_GOLD)
                .pattern(" g ")
                .pattern("gwg")
                .pattern(" g ")
                .input('g', Items.GOLD_INGOT)
                .input('w', NeMuelchItems.WATERING_CAN_IRON)
                .criterion(FabricRecipeProvider.hasItem(Items.GOLD_INGOT), FabricRecipeProvider.conditionsFromItem(Items.GOLD_INGOT))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.WATERING_CAN_IRON), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.WATERING_CAN_IRON))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.WATERING_CAN_DIAMOND)
                .pattern(" d ")
                .pattern("dwd")
                .pattern(" d ")
                .input('d', Items.DIAMOND)
                .input('w', NeMuelchItems.WATERING_CAN_GOLD)
                .criterion(FabricRecipeProvider.hasItem(Items.DIAMOND), FabricRecipeProvider.conditionsFromItem(Items.DIAMOND))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.WATERING_CAN_GOLD), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.WATERING_CAN_GOLD))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET)
                .pattern("d d")
                .pattern("d d")
                .pattern("ddd")
                .input('d', Blocks.CHISELED_DEEPSLATE)
                .criterion(FabricRecipeProvider.hasItem(Blocks.CHISELED_DEEPSLATE), FabricRecipeProvider.conditionsFromItem(Blocks.CHISELED_DEEPSLATE))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.POT_LAUNCHER_LOADER)
                .pattern("lll")
                .pattern("ppl")
                .pattern("ccc")
                .input('l', Items.LEATHER)
                .input('p', ItemTags.PLANKS)
                .input('c', Items.COPPER_INGOT)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .criterion(FabricRecipeProvider.hasItem(Items.COPPER_INGOT), FabricRecipeProvider.conditionsFromItem(Items.COPPER_INGOT))
                .criterion("has_planks", FabricRecipeProvider.conditionsFromTag(ItemTags.PLANKS))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.POT_LAUNCHER_LEGS)
                .pattern("p  ")
                .pattern("pil")
                .pattern("ppp")
                .input('p', ItemTags.PLANKS)
                .input('i', Items.IRON_NUGGET)
                .input('l', Items.LEATHER)
                .criterion(FabricRecipeProvider.hasItem(Items.LEATHER), FabricRecipeProvider.conditionsFromItem(Items.LEATHER))
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_NUGGET), FabricRecipeProvider.conditionsFromItem(Items.IRON_NUGGET))
                .criterion("has_planks", FabricRecipeProvider.conditionsFromTag(ItemTags.PLANKS))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchItems.POT_LAUNCHER)
                .pattern("bl")
                .pattern("# ")
                .input('b', NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET)
                .input('l', NeMuelchItems.POT_LAUNCHER_LOADER)
                .input('#', NeMuelchItems.POT_LAUNCHER_LEGS)
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.POT_LAUNCHER_DEEPSLATE_BASKET))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.POT_LAUNCHER_LOADER), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.POT_LAUNCHER_LOADER))
                .criterion(FabricRecipeProvider.hasItem(NeMuelchItems.POT_LAUNCHER_LEGS), FabricRecipeProvider.conditionsFromItem(NeMuelchItems.POT_LAUNCHER_LEGS))
                .offerTo(consumer);
    }

    private static void generateBlocks(Consumer<RecipeJsonProvider> consumer) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchBlocks.PESTCANE_STATION)
                .pattern("L  ")
                .pattern("CSC")
                .pattern("  L")
                .input('L', Blocks.LIGHTNING_ROD)
                .input('C', Items.COPPER_INGOT)
                .input('S', Blocks.SMITHING_TABLE)
                .criterion(FabricRecipeProvider.hasItem(Items.COPPER_INGOT), FabricRecipeProvider.conditionsFromItem(Items.COPPER_INGOT))
                .criterion(FabricRecipeProvider.hasItem(Blocks.SMITHING_TABLE), FabricRecipeProvider.conditionsFromItem(Blocks.SMITHING_TABLE))
                .criterion(FabricRecipeProvider.hasItem(Blocks.LIGHTNING_ROD), FabricRecipeProvider.conditionsFromItem(Blocks.LIGHTNING_ROD))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchBlocks.ROPER)
                .pattern("   ")
                .pattern("NLI")
                .pattern("SSS")
                .input('N', Items.IRON_NUGGET)
                .input('L', Blocks.LEVER)
                .input('I', Items.IRON_INGOT)
                .input('S', ItemTags.WOODEN_SLABS)
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT), FabricRecipeProvider.conditionsFromItem(Items.IRON_INGOT))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, NeMuelchBlocks.WALL_LANTERN)
                .pattern("ii")
                .pattern(" l")
                .input('i', Items.IRON_NUGGET)
                .input('l', Items.LANTERN)
                .criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
                .criterion(hasItem(Items.LANTERN), conditionsFromItem(Items.LANTERN))
                .offerTo(consumer);
    }

    private static void generateMeatLumps(Consumer<RecipeJsonProvider> consumer) {
        CookingRecipeJsonBuilder.createSmelting(
                        Ingredient.ofItems(NeMuelchItems.MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.COOKED_MEAT_LUMP,
                        0.6F, 600
                )
                .criterion(hasItem(NeMuelchItems.MEAT_LUMP), conditionsFromItem(NeMuelchItems.MEAT_LUMP))
                .offerTo(consumer, "cooked_meat_lump_from_smelting");
        CookingRecipeJsonBuilder.createSmelting(
                        Ingredient.ofItems(NeMuelchItems.ROTTEN_MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.MEAT_LUMP,
                        0.1F, 4800
                )
                .criterion(hasItem(NeMuelchItems.ROTTEN_MEAT_LUMP), conditionsFromItem(NeMuelchItems.ROTTEN_MEAT_LUMP))
                .offerTo(consumer, "meat_lump_from_smelting");

        CookingRecipeJsonBuilder.createSmoking(
                        Ingredient.ofItems(NeMuelchItems.MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.COOKED_MEAT_LUMP,
                        0.6F, 300
                )
                .criterion(hasItem(NeMuelchItems.MEAT_LUMP), conditionsFromItem(NeMuelchItems.MEAT_LUMP))
                .offerTo(consumer, "cooked_meat_lump_from_smoking");
        CookingRecipeJsonBuilder.createSmoking(
                        Ingredient.ofItems(NeMuelchItems.ROTTEN_MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.MEAT_LUMP,
                        0.3F, 600
                )
                .criterion(hasItem(NeMuelchItems.ROTTEN_MEAT_LUMP), conditionsFromItem(NeMuelchItems.ROTTEN_MEAT_LUMP))
                .offerTo(consumer, "meat_lump_from_smoking");

        CookingRecipeJsonBuilder.createCampfireCooking(
                        Ingredient.ofItems(NeMuelchItems.MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.COOKED_MEAT_LUMP,
                        0.1F, 100
                )
                .criterion(hasItem(NeMuelchItems.MEAT_LUMP), conditionsFromItem(NeMuelchItems.MEAT_LUMP))
                .offerTo(consumer, "cooked_meat_lump_from_campfire_cooking");
        CookingRecipeJsonBuilder.createCampfireCooking(
                        Ingredient.ofItems(NeMuelchItems.ROTTEN_MEAT_LUMP), RecipeCategory.FOOD, NeMuelchItems.MEAT_LUMP,
                        0.1F, 200
                )
                .criterion(hasItem(NeMuelchItems.ROTTEN_MEAT_LUMP), conditionsFromItem(NeMuelchItems.ROTTEN_MEAT_LUMP))
                .offerTo(consumer, "meat_lump_from_campfire_cooking");
    }

    private static void generateCrates(Consumer<RecipeJsonProvider> consumer) {
        for (CrateBlockItem crate : NeMuelchItems.CRATES) {
            if (!(crate.getBlock() instanceof CrateBlock crateBlock)) continue;
            Block baseBlock = crateBlock.getBaseMaterial();
            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, crate)
                    .pattern("   ")
                    .pattern("s s")
                    .pattern("ppp")
                    .input('s', Items.STICK)
                    .input('p', baseBlock)
                    .criterion(hasItem(Items.STICK), conditionsFromItem(Items.STICK))
                    .criterion(hasItem(baseBlock), conditionsFromItem(baseBlock))
                    .offerTo(consumer);
        }

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, NeMuelchBlocks.WATER_CRATE)
                .pattern("p p")
                .pattern("pbp")
                .pattern("ppp")
                .input('p', ItemTags.WOODEN_SLABS)
                .input('b', Items.BUCKET)
                .criterion(hasItem(Items.BUCKET), conditionsFromItem(Items.BUCKET))
                .criterion("has_wooden_slabs", conditionsFromTag(ItemTags.WOODEN_SLABS))
                .offerTo(consumer);
    }

    private static void generateMisc(Consumer<RecipeJsonProvider> consumer) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.STRING, 3)
                .input(ItemTags.WOOL)
                .criterion("has_wool", FabricRecipeProvider.conditionsFromTag(ItemTags.WOOL))
                .offerTo(consumer);
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, NeMuelchItems.DUMMY)
                .pattern("flf")
                .pattern("flf")
                .pattern("sls")
                .input('f', ItemTags.WOODEN_FENCES)
                .input('l', NeMuelchTags.Items.STRIPPED_LOGS)
                .input('s', Items.SMOOTH_STONE_SLAB)
                .criterion("has_stripped_log", conditionsFromTag(NeMuelchTags.Items.STRIPPED_LOGS))
                .criterion(hasItem(Items.SMOOTH_STONE_SLAB), conditionsFromItem(Items.SMOOTH_STONE_SLAB))
                .criterion("has_fence", conditionsFromTag(ItemTags.WOODEN_FENCES))
                .offerTo(consumer);
    }

    private static void generateTraps(Consumer<RecipeJsonProvider> consumer) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, NeMuelchBlocks.SPIKE_TRAP)
                .pattern("iii")
                .pattern("iii")
                .pattern("sss")
                .input('i', Items.IRON_NUGGET)
                .input('s', ItemTags.WOODEN_SLABS)
                .criterion(hasItem(Items.IRON_NUGGET), conditionsFromItem(Items.IRON_NUGGET))
                .criterion("has_wooden_slabs", conditionsFromTag(ItemTags.WOODEN_SLABS))
                .offerTo(consumer);
    }

    private void generateClimbingPickaxes(Consumer<RecipeJsonProvider> consumer) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, NeMuelchItems.STONE_CLIMBING_PICKAXE)
                .pattern("bbb")
                .pattern(" s ")
                .pattern("s  ")
                .input('b', ItemTags.STONE_CRAFTING_MATERIALS)
                .input('s', Items.STICK)
                .criterion("has_stones", conditionsFromTag(ItemTags.STONE_CRAFTING_MATERIALS))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, MythicMetalsItemsCompat.COPPER_CLIMBING_PICKAXE)
                .pattern("iib")
                .pattern(" s ")
                .pattern("s  ")
                .input('i', Items.COPPER_INGOT)
                .input('b', Items.COPPER_BLOCK)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.COPPER_BLOCK), conditionsFromItem(Items.COPPER_BLOCK))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, NeMuelchItems.GOLDEN_CLIMBING_PICKAXE)
                .pattern("iib")
                .pattern(" s ")
                .pattern("s  ")
                .input('i', Items.GOLD_INGOT)
                .input('b', Items.GOLD_BLOCK)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.GOLD_BLOCK), conditionsFromItem(Items.GOLD_BLOCK))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, NeMuelchItems.IRON_CLIMBING_PICKAXE)
                .pattern("iib")
                .pattern(" s ")
                .pattern("s  ")
                .input('i', Items.IRON_INGOT)
                .input('b', Items.IRON_BLOCK)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.GOLD_BLOCK), conditionsFromItem(Items.GOLD_BLOCK))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, NeMuelchItems.DIAMOND_CLIMBING_PICKAXE)
                .pattern("iib")
                .pattern(" s ")
                .pattern("s  ")
                .input('i', Items.DIAMOND)
                .input('b', Items.DIAMOND_BLOCK)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.DIAMOND_BLOCK), conditionsFromItem(Items.DIAMOND_BLOCK))
                .offerTo(consumer);

        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, NeMuelchItems.NETHERITE_CLIMBING_PICKAXE)
                .pattern("iib")
                .pattern(" s ")
                .pattern("s  ")
                .input('i', Items.NETHERITE_INGOT)
                .input('b', Items.NETHERITE_BLOCK)
                .input('s', Items.STICK)
                .criterion(hasItem(Items.NETHERITE_BLOCK), conditionsFromItem(Items.NETHERITE_BLOCK))
                .offerTo(consumer);
    }
}
