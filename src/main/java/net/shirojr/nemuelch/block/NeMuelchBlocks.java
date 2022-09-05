package net.shirojr.nemuelch.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.PestcaneStationBlock;
import net.shirojr.nemuelch.item.NeMuelchItemGroup;

public class NeMuelchBlocks {

    public static final Block PESTCANE_STATION = registerBlock("pestcane_station",
            new PestcaneStationBlock(FabricBlockSettings.of(Material.METAL).nonOpaque()
                    .strength(3f)), NeMuelchItemGroup.CANES);



    private static Block registerBlock(String name, Block block, ItemGroup group) {

        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(NeMuelch.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {

        return Registry.register(Registry.ITEM, new Identifier(NeMuelch.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings().group(group)));
    }

    public static void registerModBlocks() {

        NeMuelch.LOGGER.info("Registering " + NeMuelch.MOD_ID + " Mod blocks");
    }
}
