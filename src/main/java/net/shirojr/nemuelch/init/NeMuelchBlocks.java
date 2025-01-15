package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.*;
import net.shirojr.nemuelch.block.custom.EmitterBlocks.ParticleEmitterBlock;
import net.shirojr.nemuelch.block.custom.EmitterBlocks.SoundEmitterBlock;
import net.shirojr.nemuelch.block.custom.FogBlocks.*;
import net.shirojr.nemuelch.block.custom.StationBlocks.PestcaneStationBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.item.custom.supportItem.IronScaffoldingItem;

import java.util.function.Function;

public class NeMuelchBlocks {
    public static final Block PESTCANE_STATION = registerBlock("pestcane_station",
            new PestcaneStationBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(3f)), NeMuelchItemGroups.SUPPORT);

    public static final Block ROPER = registerBlock("roper",
            new RopeWinchBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(1f)), NeMuelchItemGroups.SUPPORT);

    public static final Block ROPE = registerBlock("rope",
            new RopeBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().collidable(false)
                    .strength(1f)), NeMuelchItemGroups.SUPPORT);

    public static final Block PARTICLE_EMITTER = registerBlock("particle_emitter",
            new ParticleEmitterBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID).
                    strength(-1.0f).dropsNothing().nonOpaque()), NeMuelchItemGroups.HELPERTOOLS);

    public static final Block SOUND_EMITTER = registerBlock("sound_emitter",
            new SoundEmitterBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID).
                    strength(-1.0f).dropsNothing().nonOpaque()), NeMuelchItemGroups.HELPERTOOLS);

    public static final Block IRON_SCAFFOLDING = registerBlockWithCustomItem("iron_scaffolding",
            new IronScaffoldingBlock(FabricBlockSettings.of(Material.DECORATION, MapColor.IRON_GRAY).noCollision()
                    .strength(3.5F).sounds(BlockSoundGroup.ANVIL).dynamicBounds()), block ->
                    new IronScaffoldingItem(block, new Item.Settings().group(NeMuelchItemGroups.SUPPORT)));

    public static final Block BLACK_FOG = registerBlock("black_fog",
            new BlackFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block WHITE_FOG = registerBlock("white_fog",
            new WhiteFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block RED_FOG = registerBlock("red_fog",
            new RedFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block BLUE_FOG = registerBlock("blue_fog",
            new BlueFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block GREEN_FOG = registerBlock("green_fog",
            new GreenFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block PURPLE_FOG = registerBlock("purple_fog",
            new PurpleFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroups.HELPERTOOLS);

    public static final Block HONEY_FLUID_BLOCK = registerBlockWithoutBlockItem("honey_fluid_block",
            new NeMuelchFluidBlock(NeMuelchFluids.HONEY_STILL, FabricBlockSettings.of(Material.WATER)
                    .noCollision().nonOpaque().dropsNothing()));

    public static final Block SLIME_FLUID_BLOCK = registerBlockWithoutBlockItem("slime_fluid_block",
            new NeMuelchFluidBlock(NeMuelchFluids.SLIME_STILL, FabricBlockSettings.of(Material.WATER)
                    .noCollision().nonOpaque().dropsNothing()));

    public static final Block WAND_OF_SOL = registerBlockWithoutBlockItem("wandofsol",
            new WandOfSolBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()));

    public static final Block WATERING_CAN = registerBlockWithoutBlockItem("watering_can",
            new WateringCanBlock(FabricBlockSettings.of(Material.METAL).nonOpaque().dropsNothing().strength(2f)));

    public static final Block DROP_POT = registerBlockWithoutBlockItem("drop_pot",
            new DropPotBlock(FabricBlockSettings.of(Material.DECORATION).mapColor(MapColor.BROWN).strength(1f)));


    private static Block registerBlock(String name, Block block, ItemGroup group) {

        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }

    private static Block registerBlockWithCustomItem(String name, Block block, Function<Block, Item> itemFactory) {
        Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name), itemFactory.apply(block));
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {

        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings().group(group)));
    }

    private static Block registerBlockWithoutBlockItem(String name, Block block) {
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }


    public static void initialize() {
        // static initialisation
    }
}
