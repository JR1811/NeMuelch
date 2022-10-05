package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.client.ArkaduscaneProjectileEntityRenderer;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.*;
import net.shirojr.nemuelch.network.EntitySpawnPacket;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.screen.PestcaneStationScreen;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

import java.util.UUID;

public class NeMuelchClient implements ClientModInitializer {

    public static final Identifier ID = NeMuelch.ENTITY_SPAWN_PACKET_ID;


    @Override
    public void onInitializeClient() {

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.ARKADUS_CANE, new ArkaduscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_CANE, new GladiuscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_BLADE, new GladiusBladeRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLOVE_ITEM, new TraininggloveRenderer());

        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PESTCANE_STATION, RenderLayer.getCutout());
        ScreenRegistry.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PARTICLE_EMITTER, RenderLayer.getTranslucent());

        EntityRendererRegistry.register(NeMuelch.ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE, ArkaduscaneProjectileEntityRenderer::new);
        NeMuelchEntities.registerEntities();
        NeMuelch.LOGGER.info("Initialized entities on the Client side (NeMuelchClient)");

        receiveEntityPacket();
    }

    public void receiveEntityPacket() {

        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {

            // reading entity data from packet buffer
            EntityType<?> entityType = Registry.ENTITY_TYPE.get(buf.readVarInt());

            UUID uuid = buf.readUuid();
            int entityId = buf.readVarInt();

            Vec3d pos = EntitySpawnPacket.PacketBufUtil.readVec3d(buf);


            client.execute(() -> {

                if (MinecraftClient.getInstance().world == null)
                    throw new IllegalStateException("Tried to spawn entity in a null world!");

                Entity e = entityType.create(MinecraftClient.getInstance().world);

                if (e == null)
                    throw new IllegalStateException("Failed to create instance of entity \"" + Registry.ENTITY_TYPE.getId(entityType) + "\"!");

                e.updateTrackedPosition(pos);
                e.setPos(pos.x, pos.y, pos.z);
                e.setId(entityId);
                e.setUuid(uuid);

                MinecraftClient.getInstance().world.addEntity(entityId, e);
            });
        });
    }
}
