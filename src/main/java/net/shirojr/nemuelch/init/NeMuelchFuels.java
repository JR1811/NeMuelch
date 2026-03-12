package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.registry.FuelRegistry;

public class NeMuelchFuels {
    static {
        FuelRegistry.INSTANCE.add(NeMuelchTags.Items.CRATES, 1500);
    }

    public static void initialize() {
        // static initialisation
    }
}
