package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;
import net.shirojr.nemuelch.sound.instance.OminousHeartSoundInstance;
import net.shirojr.nemuelch.sound.instance.TntStickItemEntitySoundInstance;
import net.shirojr.nemuelch.sound.instance.WhisperingSoundInstance;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;

public class NeMuelchS2CPacketHandler {
    public static final Identifier WATERING_CAN_PARTICLE_CHANNEL = new Identifier(NeMuelch.MOD_ID, "watering_can_fill");
    public static final Identifier SLEEP_EVENT_S2C_CHANNEL = new Identifier(NeMuelch.MOD_ID, "sleep_event_s2c");
    public static final Identifier CANCEL_SLEEP_EVENT_S2C_CHANNEL = new Identifier(NeMuelch.MOD_ID, "cancel_sleep_event_s2c");
    public static final Identifier START_SOUND_INSTANCE_CHANNEL = new Identifier(NeMuelch.MOD_ID, "start_sound_instance");


    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(WATERING_CAN_PARTICLE_CHANNEL, NeMuelchS2CPacketHandler::handleWateringCanParticlePacket);
        ClientPlayNetworking.registerGlobalReceiver(SLEEP_EVENT_S2C_CHANNEL, NeMuelchS2CPacketHandler::handleSleepEventPacket);
        ClientPlayNetworking.registerGlobalReceiver(CANCEL_SLEEP_EVENT_S2C_CHANNEL, NeMuelchS2CPacketHandler::handleCancelSleepEventPacket);
        ClientPlayNetworking.registerGlobalReceiver(START_SOUND_INSTANCE_CHANNEL, NeMuelchS2CPacketHandler::handleSoundInstancePacket);
    }

    private static void handleWateringCanParticlePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                        PacketByteBuf buf, PacketSender packetSender) {
        BlockPos target = buf.readBlockPos();

        client.execute(() -> NeMuelch.devLogger("S2C network packet received"));
    }

    private static void handleSleepEventPacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                               PacketByteBuf clientBuf, PacketSender packetSender) {
        float delayInSeconds = clientBuf.readFloat();
        BlockPos sleepingPos = clientBuf.readBlockPos();
        client.execute(() -> NeMuelchClient.clientTickHandler.startTicking(delayInSeconds, () -> {
            PacketByteBuf serverBuf = PacketByteBufs.create();
            serverBuf.writeBlockPos(sleepingPos);
            ClientPlayNetworking.send(NeMuelchC2SPacketHandler.SLEEP_EVENT_C2S_CHANNEL, serverBuf);
        }));
    }

    private static void handleCancelSleepEventPacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                     PacketByteBuf clientBuf, PacketSender packetSender) {
        client.execute(() -> NeMuelchClient.clientTickHandler.stopAndResetTicking());
    }

    private static void handleSoundInstancePacket(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler,
                                                  PacketByteBuf clientBuf, PacketSender packetSender) {
        Identifier instanceIdentifier = clientBuf.readIdentifier();
        int entityId = clientBuf.readVarInt();

        client.execute(() -> {
            if (client.world == null) return;
            SoundInstanceHelper soundInstanceHelper = SoundInstanceHelper.fromIdentifier(instanceIdentifier);
            Entity entity = client.world.getEntityById(entityId);
            if (soundInstanceHelper == null || entity == null) return;

            SoundInstance soundInstance;
            switch (soundInstanceHelper) {
                case TNT_STICK -> {
                    if (!(entity instanceof TntStickItemEntity tntStickItemEntity))
                        return; // FIXME: entity is always null?
                    soundInstance = new TntStickItemEntitySoundInstance(tntStickItemEntity);
                }
                case OMINOUS_HEART -> {
                    if (!(entity instanceof PlayerEntity playerEntity)) return;
                    soundInstance = new OminousHeartSoundInstance(playerEntity);
                }
                case WHISPERS -> {
                    if (!(entity instanceof PlayerEntity playerEntity)) return;
                    soundInstance = new WhisperingSoundInstance(playerEntity);
                }
                default -> {
                    NeMuelch.devLogger("Handling of SoundInstance packet has failed.");
                    return;
                }
            }
            if (NeMuelchClient.SOUND_INSTANCE_CACHE.containsKey(soundInstance.getId())) {
                if (NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()) instanceof WhisperingSoundInstance whisperingSoundInstance) {
                    whisperingSoundInstance.shouldFinish(true);
                } else if (NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()) != null) {
                    client.getSoundManager().stop(NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()));
                }
                NeMuelchClient.SOUND_INSTANCE_CACHE.remove(soundInstance.getId());
            }
            NeMuelchClient.SOUND_INSTANCE_CACHE.put(soundInstance.getId(), soundInstance);
            client.getSoundManager().play(soundInstance);
        });
    }
}