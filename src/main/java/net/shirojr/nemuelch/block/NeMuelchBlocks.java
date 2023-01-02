package net.shirojr.nemuelch.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.FogBlocks.*;
import net.shirojr.nemuelch.block.custom.EmitterBlocks.ParticleEmitterBlock;
import net.shirojr.nemuelch.block.custom.EmitterBlocks.SoundEmitterBlock;
import net.shirojr.nemuelch.block.custom.IronScaffoldingBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.PestcaneStationBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeBlock;
import net.shirojr.nemuelch.block.custom.StationBlocks.RopeWinchBlock;
import net.shirojr.nemuelch.item.NeMuelchItemGroup;

public class NeMuelchBlocks {

    public static final Block PESTCANE_STATION = registerBlock("pestcane_station",
            new PestcaneStationBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(3f)), NeMuelchItemGroup.SUPPORT);

    public static final Block ROPER = registerBlock("roper",
            new RopeWinchBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(1f)), NeMuelchItemGroup.SUPPORT);

    public static final Block ROPE = registerBlock("rope",
            new RopeBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(1f)), NeMuelchItemGroup.SUPPORT);


    public static final Block PARTICLE_EMITTER = registerBlock("particle_emitter",
            new ParticleEmitterBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID).
                    strength(-1.0f).dropsNothing().nonOpaque()), NeMuelchItemGroup.HELPERTOOLS);

    public static final Block SOUND_EMITTER = registerBlock("sound_emitter",
            new SoundEmitterBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID).
                    strength(-1.0f).dropsNothing().nonOpaque()), NeMuelchItemGroup.HELPERTOOLS);

    public static final Block IRON_SCAFFOLDING = registerBlock("iron_scaffolding",
            new IronScaffoldingBlock(FabricBlockSettings.of(Material.DECORATION, MapColor.IRON_GRAY).noCollision()
                    .strength(3.5F).sounds(BlockSoundGroup.ANVIL).dynamicBounds()), NeMuelchItemGroup.SUPPORT);

    public static final Block BLACK_FOG = registerBlock("black_fog",
            new BlackFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);

    public static final Block WHITE_FOG = registerBlock("white_fog",
            new WhiteFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);

    public static final Block RED_FOG = registerBlock("red_fog",
            new RedFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);

    public static final Block BLUE_FOG = registerBlock("blue_fog",
            new BlueFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);

    public static final Block GREEN_FOG = registerBlock("green_fog",
            new GreenFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);

    public static final Block PURPLE_FOG = registerBlock("purple_fog",
            new PurpleFogBlock(FabricBlockSettings.copy(Blocks.STRUCTURE_VOID)
                    .strength(-1.0f).sounds(BlockSoundGroup.SOUL_SAND).nonOpaque().noCollision()
                    .allowsSpawning((state, world, pos, type) -> false)
                    .solidBlock((state, world, pos) -> false)
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)),
            NeMuelchItemGroup.HELPERTOOLS);


    private static Block registerBlock(String name, Block block, ItemGroup group) {

        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {

        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings().group(group)));
    }

    private static Block registerBlockWithoutBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }

    public static void registerModBlocks() {
        // empty method loads in jvm and initializes NeMuelchSounds class
        NeMuelch.LOGGER.info("Registering " + NeMuelch.MOD_ID + " Mod blocks");
    }
}
