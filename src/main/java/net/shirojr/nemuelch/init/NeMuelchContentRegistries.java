package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;

public class NeMuelchContentRegistries {
    static {
        TillableBlockRegistry.register(
                Blocks.PODZOL,
                HoeItem::canTillFarmland,
                HoeItem.createTillAction(Blocks.FARMLAND.getDefaultState())
        );
    }

    public static void initialize() {
        // static initialisation
    }
}
