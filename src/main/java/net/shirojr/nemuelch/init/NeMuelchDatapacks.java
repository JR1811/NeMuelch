package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.shirojr.nemuelch.datapack.RandomTickSpeedChanceDatapack;

public class NeMuelchDatapacks {
    static {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new RandomTickSpeedChanceDatapack());
    }

    public static void initialize() {
        // static initialisation
    }
}
