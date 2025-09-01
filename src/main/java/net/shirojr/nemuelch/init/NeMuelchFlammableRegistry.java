package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.registry.tag.BlockTags;
import net.shirojr.nemuelch.block.util.VariationHolder;

public class NeMuelchFlammableRegistry {
    static {
        int chimneyFireSafety = 2;
        for (VariationHolder variation : NeMuelchBlocks.VARIATION_BLOCKS) {
            if (variation.getVariant().blockTags().contains(BlockTags.PLANKS)) {
                FlammableBlockRegistry.getDefaultInstance()
                        .add(variation.getBlock(), Math.max(0, 5 - chimneyFireSafety), Math.max(0, 20 - chimneyFireSafety));
            } else if (variation.getVariant().name().endsWith("_log")) {
                FlammableBlockRegistry.getDefaultInstance()
                        .add(variation.getBlock(), Math.max(0, 5 - chimneyFireSafety), Math.max(0, 5 - chimneyFireSafety));
            }
        }
    }

    public static void initialize() {
        // static initialisation
    }
}
