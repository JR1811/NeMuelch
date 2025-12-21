package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.entity.client.AdvancedFogBlockEntityRenderer;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;
import net.shirojr.nemuelch.camera.CameraShakeHandler;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaders;
import net.shirojr.nemuelch.entity.client.*;
import net.shirojr.nemuelch.event.custom.ClientCountdownHandler;
import net.shirojr.nemuelch.event.custom.CommandRegistrationEvents;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.item.client.AdvancedFogBlockItemRenderer;
import net.shirojr.nemuelch.network.NemuelchS2CNetworking;
import net.shirojr.nemuelch.screen.custom.PestcaneStationScreen;
import net.shirojr.nemuelch.screen.custom.RopeWinchScreen;

import java.util.HashMap;
import java.util.List;

public class NeMuelchClient implements ClientModInitializer {
    public static final ClientCountdownHandler CLIENT_COUNTDOWN_HANDLER = new ClientCountdownHandler();
    public static final HashMap<Identifier, SoundInstance> SOUND_INSTANCE_CACHE = new HashMap<>();
    public static final CameraShakeHandler CAMERA_SHAKE_HANDLER = new CameraShakeHandler();

    @Override
    public void onInitializeClient() {
        NeMuelchEntityModelLayers.initialize();
        NeMuelchEvents.initializeClient();
        NeMuelchModelPredicateProviders.initialize();
        NemuelchS2CNetworking.initialize();
        NeMuelchColorProviders.initialize();
        NeMuelchParticleFactories.initialize();
        if (NeMuelch.isSatinPresent()) {
            NeMuelchShaders.initialize();
        }
        CommandRegistrationEvents.registerClient();

        registerBlockRendering();
        registerBlockEntityRendering();
        registerEntityRendering();
        registerScreenHandlerScreens();
        registerDynamicItemRendering();

        CLIENT_COUNTDOWN_HANDLER.registerCountdown();
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

        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROTTEN_TREE_SAPLING, RenderLayer.getCutout());

        for (VariationHolder variationHolder : NeMuelchBlocks.VARIATION_BLOCKS) {
            Variation variant = variationHolder.getVariant();
            List<TagKey<Block>> tags = variant.blockTags();
            if (tags.contains(ConventionalBlockTags.GLASS_BLOCKS) || tags.contains(ConventionalBlockTags.GLASS_PANES)) {
                BlockRenderLayerMap.INSTANCE.putBlock(variationHolder.getBlock(), RenderLayer.getTranslucent());
            }
            if (tags.contains(BlockTags.TRAPDOORS) || tags.contains(BlockTags.DOORS)) {
                BlockRenderLayerMap.INSTANCE.putBlock(variationHolder.getBlock(), RenderLayer.getCutout());
            }
            if (variant.equals(NeMuelchBlockVariations.IRON_BARS)) {
                BlockRenderLayerMap.INSTANCE.putBlock(variationHolder.getBlock(), RenderLayer.getCutout());
            }
        }
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
        BlockEntityRendererFactories.register(NeMuelchBlockEntities.ADVANCED_FOG, AdvancedFogBlockEntityRenderer::new);
    }

    private static void registerScreenHandlerScreens() {
        HandledScreens.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
        HandledScreens.register(NeMuelchScreenHandlers.ROPER_SCREEN_HANDLER, RopeWinchScreen::new);
    }

    private static void registerDynamicItemRendering() {
        BuiltinItemRendererRegistry.INSTANCE.register(NeMuelchItems.ADVANCED_FOG.asItem(), new AdvancedFogBlockItemRenderer());
    }

    public static boolean isIrisModLoaded() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }
}
