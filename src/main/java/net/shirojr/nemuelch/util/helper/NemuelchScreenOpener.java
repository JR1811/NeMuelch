package net.shirojr.nemuelch.util.helper;

import net.minecraft.client.MinecraftClient;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.screen.custom.RopeModificationScreen;

/**
 * Used to guard against logical server side access to client only code in e.g. Item classes
 */
public class NemuelchScreenOpener {
    public static void openRopeModificationScreen(RopeData ropeData) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        client.setScreen(new RopeModificationScreen(ropeData));
    }
}
