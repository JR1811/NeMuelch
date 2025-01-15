package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import net.shirojr.nemuelch.network.packet.EntitySpawnPacket;
import net.shirojr.nemuelch.sound.SoundInstanceHandler;
import net.shirojr.nemuelch.sound.instance.OminousHeartSoundInstance;
import net.shirojr.nemuelch.util.LoggerUtil;
import net.shirojr.nemuelch.util.ParticlePacketType;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;

import java.util.HashMap;
import java.util.UUID;

public class NemuelchS2CNetworking {
    static {
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.WATERING_CAN_PARTICLE_S2C, NemuelchS2CNetworking::handleWateringCanParticlePacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SLEEP_EVENT_S2C, NemuelchS2CNetworking::handleSleepEventPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.CANCEL_SLEEP_EVENT_S2C, NemuelchS2CNetworking::handleCancelSleepEventPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.START_SOUND_INSTANCE_S2C, NemuelchS2CNetworking::handleSoundInstancePacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.INIT_OBFUSCATED_CACHE_S2C, NemuelchS2CNetworking::handleObfuscatedCacheInit);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.UPDATE_OBFUSCATED_CACHE_S2C, NemuelchS2CNetworking::handleObfuscatedCacheUpdate);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.UPDATE_ILLUSIONS_CACHE_S2C, NemuelchS2CNetworking::handleIllusionsCacheUpdate);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ENTITY_SPAWN_PACKET, NemuelchS2CNetworking::handleEntitySpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.PLAY_PARTICLE_S2C, NemuelchS2CNetworking::handleParticleSpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SOUND_PACKET_S2C, NemuelchS2CNetworking::handleSoundPacket);
    }

    private static void handleEntitySpawnPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
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

    private static void handleSleepEventPacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                               PacketByteBuf clientBuf, PacketSender packetSender) {
        float delayInSeconds = clientBuf.readFloat();
        BlockPos sleepingPos = clientBuf.readBlockPos();
        client.execute(() -> NeMuelchClient.clientTickHandler.startTicking(delayInSeconds, () -> {
            PacketByteBuf serverBuf = PacketByteBufs.create();
            serverBuf.writeBlockPos(sleepingPos);
            ClientPlayNetworking.send(NetworkIdentifiers.SLEEP_EVENT_C2S, serverBuf);
        }));
    }

    private static void handleCancelSleepEventPacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                     PacketByteBuf clientBuf, PacketSender packetSender) {
        client.execute(NeMuelchClient.clientTickHandler::stopAndResetTicking);
    }

    private static void handleSoundInstancePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                  PacketByteBuf clientBuf, PacketSender packetSender) {
        Identifier instanceIdentifier = clientBuf.readIdentifier();
        int entityId = clientBuf.readVarInt();
        client.execute(() -> SoundInstanceHandler.handleSoundInstancePackets(client, instanceIdentifier, entityId));
    }

    private static void handleIllusionsCacheUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean isPlayerTarget = buf.readBoolean();
        boolean isEntityIllusion = buf.readBoolean();
        int entityId = buf.readVarInt();
        client.execute(() -> {
            if (client.world == null) return;

            if (isEntityIllusion) {
                if (isPlayerTarget) {
                    NeMuelchClient.ILLUSIONS_CACHE.add(client.world.getEntityById(entityId));
                    return;
                }
            }
            NeMuelchClient.ILLUSIONS_CACHE.remove(client.world.getEntityById(entityId));
        });
    }

    private static void handleObfuscatedCacheInit(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int size = buf.readVarInt();
        HashMap<UUID, Boolean> obfuscationList = new HashMap<>();
        for (int i = 0; i < size; i++) {
            obfuscationList.put(buf.readUuid(), buf.readBoolean());
        }
        client.execute(() -> {
            NeMuelchClient.OBFUSCATED_CACHE.putAll(obfuscationList);
            if (client.player == null) return;
            NeMuelchClient.OBFUSCATED_CACHE.put(client.player.getUuid(), client.player.hasStatusEffect(NeMuelchEffects.OBFUSCATED));
        });
    }

    private static void handleObfuscatedCacheUpdate(MinecraftClient client, ClientPlayNetworkHandler handler,
                                                    PacketByteBuf buf, PacketSender sender) {
        UUID uuid = buf.readUuid();
        boolean isObfuscated = buf.readBoolean();
        client.execute(() -> {
            NeMuelchClient.OBFUSCATED_CACHE.put(uuid, isObfuscated);
            // if (client.player == null) return;
            // NeMuelchClient.OBFUSCATED_CACHE.put(client.player.getUuid(), client.player.hasStatusEffect(NeMuelchEffects.OBFUSCATED));
        });
    }


    public static void initialize() {
        // static initialisation
    }
}
