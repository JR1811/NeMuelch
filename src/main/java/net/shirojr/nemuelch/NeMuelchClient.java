package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.PestcaneRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class NeMuelchClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        NeMuelch.LOGGER.info("calling the PestcaneRenderer in NeMuelchClient class");

    }
}
