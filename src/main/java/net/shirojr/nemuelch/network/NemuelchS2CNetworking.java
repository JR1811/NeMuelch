package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.network.packet.EntitySpawnPacket;
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
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ENTITY_SPAWN_PACKET, NemuelchS2CNetworking::handleEntitySpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.PLAY_PARTICLE_S2C, NemuelchS2CNetworking::handleParticleSpawnPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SOUND_PACKET_S2C, NemuelchS2CNetworking::handleSoundPacket);
        ClientPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.POT_LAUNCHER_ACTIVATED, NemuelchS2CNetworking::activatePotLauncher);
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
        client.execute(NeMuelchClient.clientTickHandler::stopAndResetTicking);
    }

    private static void handleSoundInstancePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                  PacketByteBuf clientBuf, PacketSender packetSender) {
        Identifier instanceIdentifier = clientBuf.readIdentifier();
        int entityId = clientBuf.readVarInt();
        client.execute(() -> SoundInstanceHandler.handleSoundInstancePackets(client, instanceIdentifier, entityId));
    }

    public static void initialize() {
        // static initialisation
    }
}
