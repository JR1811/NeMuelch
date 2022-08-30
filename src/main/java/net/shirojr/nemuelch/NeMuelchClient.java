package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.ArkaduscaneRenderer;
import net.shirojr.nemuelch.item.client.PestcaneRenderer;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.screen.PestcaneStationScreen;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class NeMuelchClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.ARKADUS_CANE, new ArkaduscaneRenderer());

        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PESTCANE_STATION, RenderLayer.getCutout());
        ScreenRegistry.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
    }
}
