package net.shirojr.nemuelch.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchItemGroup {
    public static final ItemGroup NEMUELCH = FabricItemGroupBuilder.build(new Identifier(NeMuelch.MOD_ID,"muelch"),
            () -> new ItemStack(NeMuelchItems.GREEN_MUELCH));

    public static final ItemGroup HELPERTOOLS = FabricItemGroupBuilder.build(new Identifier(NeMuelch.MOD_ID,"helpertools"),
            () -> new ItemStack(NeMuelchItems.REFILLER_TOOL));

    public static final ItemGroup CANES = FabricItemGroupBuilder.build(new Identifier(NeMuelch.MOD_ID,"pestcanes"),
            () -> new ItemStack(NeMuelchItems.PEST_CANE));

}
