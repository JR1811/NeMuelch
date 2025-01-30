package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.entity.client.*;
import net.shirojr.nemuelch.event.custom.ClientTickHandler;
import net.shirojr.nemuelch.init.*;
import net.shirojr.nemuelch.network.NemuelchS2CNetworking;
import net.shirojr.nemuelch.screen.custom.ParticleEmitterBlockScreen;
import net.shirojr.nemuelch.screen.custom.PestcaneStationScreen;
import net.shirojr.nemuelch.screen.custom.RopeWinchScreen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class NeMuelchClient implements ClientModInitializer {
    public static final ClientTickHandler clientTickHandler = new ClientTickHandler();
    public static final HashMap<Identifier, SoundInstance> SOUND_INSTANCE_CACHE = new HashMap<>();
    public static final HashMap<UUID, Boolean> OBFUSCATED_CACHE = new HashMap<>();
    public static final HashSet<Entity> ILLUSIONS_CACHE = new HashSet<>();

    @Override
    public void onInitializeClient() {
        NeMuelchEntityModelLayers.initialize();
        NeMuelchEvents.initializeClient();
        NeMuelchGeckoLibRendering.initialize();
        NeMuelchModelPredicateProviders.initialize();
        NemuelchS2CNetworking.initialize();

        registerBlockRendering();
        registerEntityRendering();
        registerFluidRendering();
        registerScreenHandlerScreens();

        clientTickHandler.registerCountdown();
    }

    private static void registerBlockRendering() {
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PESTCANE_STATION, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PARTICLE_EMITTER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.SOUND_EMITTER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.IRON_SCAFFOLDING, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLACK_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.WHITE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.RED_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLUE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PURPLE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.GREEN_FOG, RenderLayer.getTranslucent());
    }

    private static void registerEntityRendering() {
        EntityRendererRegistry.register(NeMuelchEntities.DROP_POT, DropPotEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.POT_LAUNCHER, PotLauncherEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.LIFT_PLATFORM, LiftPlatformRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.ONION, OnionRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.ARKADUSCANE_PROJECTILE, ArkaduscaneProjectileEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.SLIME_ITEM, SlimeItemEntityRenderer::new);
    }

    private static void registerScreenHandlerScreens() {
        HandledScreens.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
        HandledScreens.register(NeMuelchScreenHandlers.ROPER_SCREEN_HANDLER, RopeWinchScreen::new);
        HandledScreens.register(NeMuelchScreenHandlers.PARTICLE_EMITTER_SCREEN_HANDLER, ParticleEmitterBlockScreen::new);
    }

    private static void registerFluidRendering() {
        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluids.HONEY_STILL,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0xe9860c));

        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluids.HONEY_FLOWING,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0xe9860c));

        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluids.SLIME_STILL,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0x7DB367));
        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluids.SLIME_FLOWING,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0x7DB367));
    }
}
