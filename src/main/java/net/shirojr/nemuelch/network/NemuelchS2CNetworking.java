package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.block.custom.RottenMeatBlock;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.camera.DisplacementSequence;
import net.shirojr.nemuelch.client.NeMuelchClientCache;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.compat.satin.util.NetworkingParameter;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;
import net.shirojr.nemuelch.network.packet.EntitySpawnPacket;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.network.util.NetworkUtil;
import net.shirojr.nemuelch.render.TalismanChargeRenderer;
import net.shirojr.nemuelch.sound.SoundData;
import net.shirojr.nemuelch.sound.SoundInstanceHandler;
import net.shirojr.nemuelch.sound.instance.OminousHeartSoundInstance;
import net.shirojr.nemuelch.util.ParticlePacketType;
import net.shirojr.nemuelch.util.logger.LoggerUtil;

import java.util.UUID;

@SuppressWarnings("unused")
public class NemuelchS2CNetworking {
    static {
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.WATERING_CAN_PARTICLE_S2C, NemuelchS2CNetworking::handleWateringCanParticlePacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CANCEL_SLEEP_EVENT_S2C, NemuelchS2CNetworking::handleCancelSleepEventPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.START_SOUND_INSTANCE_S2C, NemuelchS2CNetworking::handleSoundInstancePacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ENTITY_SPAWN, NemuelchS2CNetworking::handleEntitySpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.PLAY_PARTICLE_S2C, NemuelchS2CNetworking::handleParticleSpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SOUND_PACKET_S2C, NemuelchS2CNetworking::handleSoundPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.POT_LAUNCHER_ACTIVATED, NemuelchS2CNetworking::activatePotLauncher);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.TALISMAN_DISCARD_PROJECTILE, NemuelchS2CNetworking::handleTalismanChargeData);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SPAWN_ROTTEN_PARTICLE, NemuelchS2CNetworking::spawnRottenParticles);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.THIRD_PERSON_ITEM_RENDERING, NemuelchS2CNetworking::cacheItemRenderingGamerule);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_START, NemuelchS2CNetworking::handleCameraDisplacementSequenceStart);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_START_SCALED, NemuelchS2CNetworking::handleCameraDisplacementSequenceStartScaled);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_STOP, NemuelchS2CNetworking::handleCameraDisplacementSequenceStop);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_STOP_ALL, NemuelchS2CNetworking::handleCameraDisplacementSequenceStopAll);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.START_FOLLOWING_SOUND_INSTANCE, NemuelchS2CNetworking::handleStartFollowingSoundInstance);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.STOP_FOLLOWING_SOUND_INSTANCE, NemuelchS2CNetworking::handleStopFollowingSoundInstance);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ADVANCED_FOG_SYNC, NemuelchS2CNetworking::handleAdvancedFogDataSync);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.DEEP_WATER_BOAT_ENDURANCE_SYNC, NemuelchS2CNetworking::handleDeepWaterBoatEndurance);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.PULL_UP_VERT_STRENGTH_GAMERULE_SYNC, NemuelchS2CNetworking::handlePullUpVertStrength);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.STRING_TO_CLIENT_CLIPBOARD, NemuelchS2CNetworking::handleClientClipboard);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.FADE_TO_BLACK, NemuelchS2CNetworking::handleFadeToBlack);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.FADE_FROM_BLACK, NemuelchS2CNetworking::handleFadeFromBlack);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.FADE_STATIC, NemuelchS2CNetworking::handleConstantFade);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CRIMSON_STATIC, NemuelchS2CNetworking::handleConstantCrimson);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.GENERAL_SHADER_PARAMETER_SYNC, NemuelchS2CNetworking::handleShaderParameterChange);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private static void handleShaderParameterChange(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        float parameterValue = buf.readFloat();
        NetworkingParameter parameterType = NetworkingParameter.values()[buf.readVarInt()];

        client.execute(() -> {
            switch (parameterType) {
            }
        });
    }

    private static void handleClientClipboard(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        String input = buf.readString();
        client.execute(() -> client.keyboard.setClipboard(input));
    }

    private static void handlePullUpVertStrength(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        double strength = buf.readDouble();
        client.execute(() -> NeMuelchClientCache.pullUpVertStrength = strength);
    }

    private static void handleDeepWaterBoatEndurance(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int ticks = buf.readVarInt();
        client.execute(() -> NeMuelchClientCache.boatDeepWaterEnduranceTicks = ticks);
    }

    private static void handleAdvancedFogDataSync(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos blockEntityPos = BlockPos.fromLong(buf.readLong());
        AdvancedFogBlockEntity.Data newData = AdvancedFogBlockEntity.Data.fromPacketByteBuf(buf);
        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null) return;
            if (!(world.getBlockEntity(blockEntityPos) instanceof AdvancedFogBlockEntity blockEntity)) return;
            blockEntity.setData(newData, true);
        });
    }

    private static void handleStopFollowingSoundInstance(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Identifier soundId = buf.readIdentifier();
        client.execute(() -> SoundInstanceHandler.handleStopSoundInstancePacket(client, soundId));
    }

    private static void handleStartFollowingSoundInstance(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int entityId = buf.readVarInt();
        SoundData soundData = SoundData.fromPacketByteBuf(buf);

        client.execute(() -> SoundInstanceHandler.handleStartSoundInstancePacket(client, soundData, entityId));
    }

    private static void handleCameraDisplacementSequenceStopAll(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(() -> NeMuelchClientCache.CAMERA_SHAKE_HANDLER.setActiveDisplacementSequence(null));
    }

    private static void handleCameraDisplacementSequenceStop(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Identifier identifier = buf.readIdentifier();

        client.execute(() -> {
            if (client.world == null) return;
            DisplacementSequence sequence = DisplacementSequence.fromRegistry(identifier, client.world.getScoreboard());
            NeMuelchClientCache.CAMERA_SHAKE_HANDLER.setActiveDisplacementSequence(null);
        });
    }

    private static void handleCameraDisplacementSequenceStartScaled(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Identifier identifier = buf.readIdentifier();
        double normalizedIntensity = MathHelper.clamp(buf.readDouble(), 0, 1);

        client.execute(() -> {
            if (client.world == null) return;
            DisplacementSequence sequence = DisplacementSequence
                    .fromRegistry(identifier, client.world.getScoreboard())
                    .getIntensityScaledCopy(normalizedIntensity);
            NeMuelchClientCache.CAMERA_SHAKE_HANDLER.setActiveDisplacementSequence(sequence);
        });
    }

    private static void handleCameraDisplacementSequenceStart(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Identifier identifier = buf.readIdentifier();

        client.execute(() -> {
            if (client.world == null) return;
            DisplacementSequence sequence = DisplacementSequence.fromRegistry(identifier, client.world.getScoreboard());
            NeMuelchClientCache.CAMERA_SHAKE_HANDLER.setActiveDisplacementSequence(sequence);
        });
    }

    private static void handleConstantCrimson(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        float intensity = buf.readFloat();
        client.execute(() -> NeMuelchShaderManager.CRIMSON_PHASE.setInstant(intensity));
    }

    private static void handleConstantFade(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        float fadeValue = buf.readFloat();
        client.execute(() -> NeMuelchShaderManager.FADE.setInstant(fadeValue));
    }

    private static void handleFadeFromBlack(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int duration = buf.readVarInt();
        client.execute(() -> NeMuelchShaderManager.FADE.fadeFromBlack(duration));
    }

    private static void handleFadeToBlack(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int duration = buf.readVarInt();
        client.execute(() -> NeMuelchShaderManager.FADE.fadeToBlack(duration));
    }

    private static void cacheItemRenderingGamerule(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean gameruleValue = buf.readBoolean();
        client.execute(() -> ThirdPersonInvisible.GameruleCache.INSTANCE.set(gameruleValue));
    }

    private static void spawnRottenParticles(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int amount = buf.readVarInt();
        int range = buf.readVarInt();
        BlockPos blockPos = BlockPos.fromLong(buf.readLong());

        client.execute(() -> {
            ClientWorld world = handler.getWorld();
            RottenMeatBlock.spawnClientParticles(amount, range, blockPos, world, world.getRandom());
        });
    }

    private static void handleTalismanChargeData(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Vec3d userPos = NetworkUtil.readVec3d(buf);
        int projectileId = buf.readVarInt();
        ItemStack stack = buf.readItemStack();

        client.execute(() -> {
            ClientWorld world = client.world;
            if (world == null || !(world.getEntityById(projectileId) instanceof ProjectileEntity projectile)) return;
            TalismanChargeRenderer renderer = TalismanChargeRenderer.getInstance();
            if (renderer.inProgress(projectile)) return;
            renderer.getRenderData().add(new TalismanChargeRenderer.Data(projectile, userPos, stack));
        });
    }

    private static void activatePotLauncher(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Entity entity = handler.getWorld().getEntityById(buf.readVarInt());
        boolean active = buf.readBoolean();
        client.execute(() -> {
            if (!(entity instanceof PotLauncherEntity potLauncher)) return;
            potLauncher.setActive(active);
        });
    }

    private static void handleEntitySpawnPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(buf.readVarInt());
        UUID uuid = buf.readUuid();
        int entityId = buf.readVarInt();
        Vec3d pos = EntitySpawnPacket.PacketBufUtil.readVec3d(buf);

        client.execute(() -> {
            if (MinecraftClient.getInstance().world == null)
                throw new IllegalStateException("Tried to spawn entity in a null world!");

            Entity e = entityType.create(MinecraftClient.getInstance().world);
            if (e == null)
                throw new IllegalStateException("Failed to create instance of entity \"" + Registries.ENTITY_TYPE.getId(entityType) + "\"!");

            e.updateTrackedPosition(pos.x, pos.y, pos.z);
            e.setPos(pos.x, pos.y, pos.z);
            e.setId(entityId);
            e.setUuid(uuid);

            MinecraftClient.getInstance().world.addEntity(entityId, e);
        });
    }

    private static void handleParticleSpawnPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos pos = buf.readBlockPos();
        ParticlePacketType particleSetting = buf.readEnumConstant(ParticlePacketType.class);
        client.execute(() -> {
            if (client.world == null) return;
            switch (particleSetting) {
                case EFFECT_PLAYTHING_OF_THE_UNSEEN_DEITY -> {
                    MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 1.0, pos.getZ(), 0.0, 0.0, 0.0);
                    MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.ENCHANT, pos.getX(), pos.getY() + 1.0, pos.getZ(), 0.0, 0.0, 0.0);
                }
                case ITEM_CALLOFAGONY_KNOCKBACK -> {
                    for (int i = 0; i < 10; i++) {
                        double x = (client.world.getRandom().nextGaussian() * 2) * pos.getX();
                        double y = (client.world.getRandom().nextGaussian() * 2) * (pos.getY() + 1.0);
                        double z = (client.world.getRandom().nextGaussian() * 2) * pos.getZ();
                        MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.ENCHANT,
                                x, y, z, 0.0, 0.0, 0.0);
                    }
                }
            }
        });
    }

    private static void handleSoundPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        BlockPos target = buf.readBlockPos();
        client.execute(() -> {
            if (MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().player != null) {
                client.getSoundManager().play(new OminousHeartSoundInstance(client.player));
            }
        });
    }

    //TODO: remove?
    private static void handleWateringCanParticlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                        PacketByteBuf buf, PacketSender packetSender) {
        BlockPos target = buf.readBlockPos();

        client.execute(() -> LoggerUtil.devLogger("S2C network packet received"));
    }

    private static void handleCancelSleepEventPacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                     PacketByteBuf clientBuf, PacketSender packetSender) {
        client.execute(NeMuelchClientCache.CLIENT_COUNTDOWN_HANDLER::stopAndResetTicking);
    }

    private static void handleSoundInstancePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                  PacketByteBuf clientBuf, PacketSender packetSender) {
        Identifier instanceIdentifier = clientBuf.readIdentifier();
        int entityId = clientBuf.readVarInt();
        client.execute(() -> SoundInstanceHandler.handleDynamicSoundInstancePackets(client, instanceIdentifier, entityId));
    }

    public static void initialize() {
        // static initialisation
    }
}
