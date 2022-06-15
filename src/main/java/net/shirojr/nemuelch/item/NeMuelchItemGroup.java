package net.shirojr.nemuelch.item;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;

public class NeMuelchItemGroup {
    public static final ItemGroup NEMUELCH = FabricItemGroupBuilder.build(new Identifier(NeMuelch.MOD_ID,"muelch"),
            () -> new ItemStack(NeMuelchItems.GREEN_MUELCH));

    //TODO: Add Group for helper items like ReFiller Tool
}
