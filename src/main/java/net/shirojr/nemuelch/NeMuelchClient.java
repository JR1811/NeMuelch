package net.shirojr.nemuelch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.client.ArkaduscaneProjectileEntityRenderer;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.fluid.NeMuelchFluid;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.client.*;
import net.shirojr.nemuelch.network.EntitySpawnPacket;
import net.shirojr.nemuelch.screen.NeMuelchScreenHandlers;
import net.shirojr.nemuelch.screen.PestcaneStationScreen;
import net.shirojr.nemuelch.screen.RopeWinchScreen;
import net.shirojr.nemuelch.sound.OminousHeartSoundInstance;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class NeMuelchClient implements ClientModInitializer {

    public static final Identifier ID = NeMuelch.ENTITY_SPAWN_PACKET_ID;

    @Override
    public void onInitializeClient() {

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.PEST_CANE, new PestcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.ARKADUS_CANE, new ArkaduscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_CANE, new GladiuscaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLADIUS_BLADE, new GladiusBladeRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.RADIATUM_CANE, new RadiatumcaneRenderer());
        GeoItemRenderer.registerItemRenderer(NeMuelchItems.GLOVE_ITEM, new TraininggloveRenderer());

        GeoItemRenderer.registerItemRenderer(NeMuelchItems.FORTIFIED_SHIELD, new FortifiedShieldRenderer());
        ModelPredicateProviderRegistry.register(NeMuelchItems.FORTIFIED_SHIELD, new Identifier("blocking"),
                (itemStack, clientWorld, livingEntity, seed) ->
                        livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F);

        GeoArmorRenderer.registerArmorRenderer(new PortableBarrelRenderer(), NeMuelchItems.PORTABLE_BARREL);

        //BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PORTABLE_BARREL, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PESTCANE_STATION, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.ROPE, RenderLayer.getCutout());



        ScreenRegistry.register(NeMuelchScreenHandlers.PESTCANE_STATION_SCREEN_HANDLER, PestcaneStationScreen::new);
        ScreenRegistry.register(NeMuelchScreenHandlers.ROPER_SCREEN_HANDLER, RopeWinchScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PARTICLE_EMITTER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.SOUND_EMITTER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.IRON_SCAFFOLDING, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLACK_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.WHITE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.RED_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.BLUE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.PURPLE_FOG, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(NeMuelchBlocks.GREEN_FOG, RenderLayer.getTranslucent());

        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluid.HONEY_STILL,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0xe9860c));
        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluid.HONEY_FLOWING,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0xe9860c));

        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluid.SLIME_STILL,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0x387849));
        FluidRenderHandlerRegistry.INSTANCE.register(NeMuelchFluid.SLIME_FLOWING,
                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                        SimpleFluidRenderHandler.WATER_FLOWING,
                        SimpleFluidRenderHandler.WATER_OVERLAY, 0x387849));

        //1. #5b7a4e
        //2. #227008


        EntityRendererRegistry.register(NeMuelch.ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE, ArkaduscaneProjectileEntityRenderer::new);

        NeMuelchEntities.registerEntities();

        // networking
        receiveEntityPacket();
        receiveParticlePacket();
        receiveSoundPacket();
    }

    /**
     * Entity communication between server and client.<br>
     * Order:<br>
     * 1. EntityType as int (buf.readVarInt())<br>
     * 2. UUID as UUID (buf.readUuid()<br>
     * 3. Entity id as Int (buf.readVarInt()<br>
     * 4. Pos as Vec3d ({@link EntitySpawnPacket EntitySpawnPacket})
     */
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

    /**
     * Particle communication between server and client.<br>
     * Order:<br>
     * 1. BlockPos<br>
     * 2. Type of Particle for rendering (check {@link NeMuelchClient.ParticlePacketType ParticlepacketType-Enum})<br>
     */
    public void receiveParticlePacket() {
        ClientSidePacketRegistry.INSTANCE.register(NeMuelch.PLAY_PARTICLE_PACKET_ID, (packetContext, attachedData) -> {
            // network thread area
            BlockPos pos = attachedData.readBlockPos();
            ParticlePacketType particleSetting = attachedData.readEnumConstant(ParticlePacketType.class);
            packetContext.getTaskQueue().execute(() -> {

                // main thread area
                switch (particleSetting) {
                    case EFFECT_PLAYTHING_OF_THE_UNSEEN_DEITY -> {
                        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 1.0, pos.getZ(), 0.0, 0.0, 0.0);
                        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.ENCHANT, pos.getX(), pos.getY() + 1.0, pos.getZ(), 0.0, 0.0, 0.0);
                    }
                    case ITEM_CALLOFAGONY_KNOCKBACK  -> {
                        for (int i = 0; i < 10; i++) {
                            double x = (MinecraftClient.getInstance().world.getRandom().nextGaussian() * 2) * pos.getX();
                            double y = (MinecraftClient.getInstance().world.getRandom().nextGaussian() * 2) * (pos.getY() + 1.0);
                            double z = (MinecraftClient.getInstance().world.getRandom().nextGaussian() * 2) * pos.getZ();
                            MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.ENCHANT,
                                    x, y, z, 0.0, 0.0, 0.0);    //FIXME: still doesn't display when item has been used :I
                        }

                    }
                }
            });
        });
    }

    public enum ParticlePacketType {
        EFFECT_PLAYTHING_OF_THE_UNSEEN_DEITY,
        ITEM_CALLOFAGONY_KNOCKBACK
    }

    /***
     * Benuttzt in: {@link net.shirojr.nemuelch.item.custom.supportItem.OminousHeartItem OminousHeart}
     */
    public void receiveSoundPacket() {
        ClientPlayNetworking.registerGlobalReceiver(NeMuelch.SOUND_PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();

            client.execute(() -> {
                if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                    client.getSoundManager().play(new OminousHeartSoundInstance(client.player));
                }
            });
        });

        /*ClientSidePacketRegistry.INSTANCE.register(NeMuelch.SOUND_PACKET_ID, (packetContext, attachedData) -> {


            packetContext.getTaskQueue().execute(() -> {
                if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.playSound(NeMuelchSounds.ITEM_OMINOUS_HEART, 2f, 1f);
                }
            });
        });*/
    }
}
