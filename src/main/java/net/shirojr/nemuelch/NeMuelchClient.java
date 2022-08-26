package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.shirojr.nemuelch.entity.Client.JuggernautRenderer;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.PestcaneRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class NeMuelchClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        NeMuelch.LOGGER.info("calling the PestcaneRenderer in NeMuelchClient class");

        EntityRendererRegistry.register(NeMuelchEntities.JUGGERNAUT, JuggernautRenderer::new);

    }
}
