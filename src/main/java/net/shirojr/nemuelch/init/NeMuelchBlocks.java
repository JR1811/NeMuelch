package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.*;
import net.shirojr.nemuelch.block.custom.StationBlocks.PestcaneStationBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface NeMuelchBlocks {
    List<Block> ALL_BLOCKS = new ArrayList<>();
    List<Block> FOG_BLOCKS = new ArrayList<>();
    List<VariationHolder> VARIATION_BLOCKS = new ArrayList<>();

    PestcaneStationBlock PESTCANE_STATION = register("pestcane_station",
            new PestcaneStationBlock(AbstractBlock.Settings.create()
                    .strength(3f)
            ), true);

    RopeWinchBlock ROPER = register("roper",
            new RopeWinchBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .strength(1f)
            ), true);

    RopeBlock ROPE = register("rope",
            new RopeBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .collidable(false)
                    .strength(1f)
                    .ticksRandomly()
            ), true);

    IronScaffoldingBlock IRON_SCAFFOLDING = register("iron_scaffolding",
            new IronScaffoldingBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(3.5F)
                    .sounds(BlockSoundGroup.ANVIL)
                    .dynamicBounds()
            ), false);

    FogBlock BLACK_FOG = registerFog("black_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock WHITE_FOG = registerFog("white_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock RED_FOG = registerFog("red_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock YELLOW_FOG = registerFog("yellow_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock BLUE_FOG = registerFog("blue_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock GREEN_FOG = registerFog("green_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    FogBlock PURPLE_FOG = registerFog("purple_fog",
            new FogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    WandOfSolBlock WAND_OF_SOL = register("wandofsol",
            new WandOfSolBlock(FabricBlockSettings.create()
                    .nonOpaque()
            ), false);

    WateringCanBlock WATERING_CAN = register("watering_can",
            new WateringCanBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .dropsNothing()
                    .strength(2f)
            ), false);

    DropPotBlock DROP_POT = register("drop_pot",
            new DropPotBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.BROWN)
                    .strength(1f)
            ), false);

    List<ChimneyBlock> CHIMNEYS = registerVariationBlocks(
            "chimney",
            (variant) -> FabricBlockSettings.copy(variant.parentBlock()),
            ChimneyBlock::new
    );

    List<PlateBlock> PLATES = registerVariationBlocks(
            "plate",
            (variant) -> FabricBlockSettings.copy(variant.parentBlock()),
            PlateBlock::new
    );

    List<DoublePlatesBlock> DOUBLE_PLATES = registerVariationBlocks(
            "double_plates",
            variation -> FabricBlockSettings.copy(variation.parentBlock()),
            DoublePlatesBlock::new
    );

    List<HalfSlabBlock> HALF_SLABS = registerVariationBlocks(
            "half_slab",
            variation -> FabricBlockSettings.copy(variation.parentBlock()),
            HalfSlabBlock::new
    );

    List<VerticalHalfSlabBlock> VERTICAL_HALF_SLABS = registerVariationBlocks(
            "vertical_half_slab",
            variation -> FabricBlockSettings.copy(variation.parentBlock()),
            VerticalHalfSlabBlock::new
    );

    List<CenteredVerticalHalfSlabBlock> CENTERED_VERTICAL_HALF_SLABS = registerVariationBlocks(
            "centered_vertical_half_slab",
            variation -> FabricBlockSettings.copy(variation.parentBlock()),
            CenteredVerticalHalfSlabBlock::new
    );

    List<CenteredHalfSlab> CENTERED_HALF_SLABS = registerVariationBlocks(
            "centered_half_slab",
            variation -> FabricBlockSettings.copy(variation.parentBlock()),
            CenteredHalfSlab::new
    );


    static <T extends Block> T register(String name, T entry, boolean registerDefaultItem, List<List<Item>> itemLists) {
        T registeredEntry = Registry.register(Registries.BLOCK, NeMuelch.getId(name), entry);
        if (registerDefaultItem) {
            BlockItem registeredItemEntry = Registry.register(Registries.ITEM, NeMuelch.getId(name), new BlockItem(registeredEntry, new Item.Settings()));
            for (List<Item> list : itemLists) {
                list.add(registeredItemEntry);
            }
        }
        ALL_BLOCKS.add(registeredEntry);
        return registeredEntry;
    }

    static <T extends Block> T register(String name, T entry, boolean registerDefaultItem) {
        return register(name, entry, registerDefaultItem, List.of(NeMuelchItems.NEMUELCH_ITEMS));
    }

    private static <T extends TransparentBlock> T registerFog(String name, T block) {
        T registeredBlock = register(name, block, true);
        FOG_BLOCKS.add(registeredBlock);
        return registeredBlock;
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Block & VariationHolder> List<T> registerVariationBlocks(
            String nameSuffix, Function<Variation, AbstractBlock.Settings> settings, BiFunction<AbstractBlock.Settings, Variation, T> blockFactory) {
        List<T> result = new ArrayList<>();
        for (Variation variant : NeMuelchBlockVariations.ALL_VARIATIONS) {
            AbstractBlock.Settings blockSettings = settings.apply(variant);

            //FIXME: this is a hacky fix ngl...
            //  Settings which use Properties, which the variation block doesn't have need to get changed
            //  Example would be Log Blocks which use AXIS Properties for map colors
            if (variant.parentBlock().getDefaultState().contains(Properties.AXIS)) {
                blockSettings = blockSettings.mapColor(MapColor.BLACK);
            }
            if (variant.parentBlock() instanceof RedstoneOreBlock) {
                blockSettings = blockSettings.luminance(value -> 0);
            }

            T registeredBlock = register(
                    variant.name().toLowerCase(Locale.ROOT) + "_" + nameSuffix,
                    blockFactory.apply(blockSettings, variant),
                    true,
                    List.of(NeMuelchItems.NEMUELCH_VARIATION_BLOCK_ITEMS)
            );
            result.add(registeredBlock);
            VARIATION_BLOCKS.add(registeredBlock);
        }
        return result;
    }


    static void initialize() {
        // static initialisation
    }
}
