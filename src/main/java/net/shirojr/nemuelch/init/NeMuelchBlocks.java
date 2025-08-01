package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.*;
import net.shirojr.nemuelch.block.custom.StationBlocks.PestcaneStationBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;

import java.util.ArrayList;
import java.util.List;

public interface NeMuelchBlocks {
    List<Block> ALL_BLOCKS = new ArrayList<>();
    List<Block> FOG_BLOCKS = new ArrayList<>();

    PestcaneStationBlock PESTCANE_STATION = register("pestcane_station",
            new PestcaneStationBlock(AbstractBlock.Settings.create()
                    .strength(3f)
            ), true);

    Block ROPER = register("roper",
            new RopeWinchBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .strength(1f)
            ), true);

    Block ROPE = register("rope",
            new RopeBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .collidable(false)
                    .strength(1f)
                    .ticksRandomly()
            ), true);

    Block IRON_SCAFFOLDING = register("iron_scaffolding",
            new IronScaffoldingBlock(FabricBlockSettings.create()
                    .noCollision()
                    .strength(3.5F)
                    .sounds(BlockSoundGroup.ANVIL)
                    .dynamicBounds()
            ), false);

    Block BLACK_FOG = registerFog("black_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block WHITE_FOG = registerFog("white_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block RED_FOG = registerFog("red_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block BLUE_FOG = registerFog("blue_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block GREEN_FOG = registerFog("green_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block PURPLE_FOG = registerFog("purple_fog",
            new TransparentBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning(Blocks::never)
                    .solidBlock(Blocks::never)
                    .suffocates(Blocks::never)
                    .blockVision(Blocks::never)
            )
    );

    Block HONEY_FLUID_BLOCK = register("honey_fluid_block",
            new NeMuelchFluidBlock(NeMuelchFluids.HONEY_STILL, FabricBlockSettings.create()
                    .noCollision()
                    .nonOpaque()
                    .dropsNothing()
            ), false);

    Block SLIME_FLUID_BLOCK = register("slime_fluid_block",
            new NeMuelchFluidBlock(NeMuelchFluids.SLIME_STILL, FabricBlockSettings.create()
                    .noCollision()
                    .nonOpaque()
                    .dropsNothing()
            ), false);

    Block WAND_OF_SOL = register("wandofsol",
            new WandOfSolBlock(FabricBlockSettings.create()
                    .nonOpaque()
            ), false);

    Block WATERING_CAN = register("watering_can",
            new WateringCanBlock(FabricBlockSettings.create()
                    .nonOpaque()
                    .dropsNothing()
                    .strength(2f)
            ), false);

    Block DROP_POT = register("drop_pot",
            new DropPotBlock(FabricBlockSettings.create()
                    .mapColor(MapColor.BROWN)
                    .strength(1f)
            ), false);


    static <T extends Block> T register(String name, T entry, boolean registerDefaultItem) {
        T registeredEntry = Registry.register(Registries.BLOCK, NeMuelch.getId(name), entry);
        if (registerDefaultItem) {
            BlockItem registeredItemEntry = Registry.register(Registries.ITEM, NeMuelch.getId(name), new BlockItem(registeredEntry, new Item.Settings()));
            NeMuelchItems.ALL_ITEMS.add(registeredItemEntry);
        }
        ALL_BLOCKS.add(registeredEntry);
        return registeredEntry;
    }

    private static Block registerFog(String name, Block block) {
        Block registeredBlock = register(name, block, true);
        FOG_BLOCKS.add(registeredBlock);
        return registeredBlock;
    }


    static void initialize() {
        // static initialisation
    }
}
