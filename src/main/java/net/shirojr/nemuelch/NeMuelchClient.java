package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.shirojr.nemuelch.block.entity.client.CrateBlockEntityRenderer;
import net.shirojr.nemuelch.block.entity.client.WaterCrateBlockEntityRenderer;
import net.shirojr.nemuelch.client.NeMuelchClientCache;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.entity.client.*;
import net.shirojr.nemuelch.event.custom.CommandRegistrationEvents;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.item.client.AdvancedFogBlockItemRenderer;
import net.shirojr.nemuelch.item.client.ChainedMaceItemRenderer;
import net.shirojr.nemuelch.network.NemuelchS2CNetworking;
import net.shirojr.nemuelch.screen.custom.PestcaneStationScreen;
import net.shirojr.nemuelch.screen.custom.RopeWinchScreen;

public class NeMuelchClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        NeMuelchEntityModelLayers.initialize();
        NeMuelchEvents.initializeClient();
        NeMuelchModelPredicateProviders.initialize();
        NemuelchS2CNetworking.initialize();
        NeMuelchColorProviders.initialize();
        NeMuelchParticleFactories.initialize();
        if (NeMuelch.isSatinModLoaded()) {
            NeMuelchShaderManager.initialize();
        }
        CommandRegistrationEvents.registerClient();

        registerBlockRendering();
        registerBlockEntityRendering();
        registerEntityRendering();
        registerScreenHandlerScreens();
        registerDynamicItemRendering();

        NeMuelchClientCache.CLIENT_COUNTDOWN_HANDLER.registerCountdown();
    }

    private static void registerBlockRendering() {
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PESTCANE_STATION, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.IRON_SCAFFOLDING, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLACK_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.WHITE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.RED_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.YELLOW_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLUE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PURPLE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.GREEN_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.WALL_LANTERN, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ADVANCED_FOG, RenderLayer.getTranslucent());

        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROTTEN_TREE_SAPLING, RenderLayer.getCutout());
    }

    private static void registerEntityRendering() {
        EntityRendererRegistry.register(NeMuelchEntities.DROP_POT, DropPotEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.POT_LAUNCHER, PotLauncherEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.LIFT_PLATFORM, LiftPlatformRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.ARKADUSCANE_PROJECTILE, ArkaduscaneProjectileEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.SLIME_ITEM, SlimeItemEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.DUMMY_CQC, DummyCloseQuarterEntityRenderer::new);
    }

    private static void registerBlockEntityRendering() {
        // BlockEntityRendererFactories.register(NeMuelchBlockEntities.ADVANCED_FOG, AdvancedFogBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(NeMuelchBlockEntities.CRATE, CrateBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(NeMuelchBlockEntities.WATER_CRATE, WaterCrateBlockEntityRenderer::new);
    }

    private static void registerScreenHandlerScreens() {
        HandledScreens.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
        HandledScreens.register(NeMuelchScreenHandlers.ROPER_SCREEN_HANDLER, RopeWinchScreen::new);
    }

    private static void registerDynamicItemRendering() {
        BuiltinItemRendererRegistry.INSTANCE.register(NeMuelchItems.ADVANCED_FOG, new AdvancedFogBlockItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(NeMuelchItems.CHAINED_MACE, new ChainedMaceItemRenderer());
    }
}
