package net.shirojr.nemuelch.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.sound.instance.DropPotFlyingSoundInstance;
import net.shirojr.nemuelch.sound.instance.FollowingRepeatableSoundInstance;
import net.shirojr.nemuelch.sound.instance.OminousHeartSoundInstance;
import net.shirojr.nemuelch.sound.instance.WhisperingSoundInstance;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class SoundInstanceHandler {
    private static final HashSet<SoundInstance> trackedSoundInstances = new HashSet<>();

    public static void handleStopSoundInstancePacket(MinecraftClient client, @Nullable Identifier soundId) {
        HashSet<SoundInstance> oldInstanceSet = new HashSet<>(trackedSoundInstances);
        for (SoundInstance entry : oldInstanceSet) {
            if (soundId != null && !entry.getId().equals(soundId)) continue;
            trackedSoundInstances.remove(entry);
            if (entry instanceof FollowingRepeatableSoundInstance followingRepeatableSoundInstance) {
                followingRepeatableSoundInstance.setRepeatCounter(followingRepeatableSoundInstance.getMaxRepeats());
            }
            client.getSoundManager().stop(entry);
        }
    }

    public static void handleStartSoundInstancePacket(MinecraftClient client, SoundData data, int entityId) {
        ClientWorld world = client.world;
        if (world == null) return;
        Entity entity = world.getEntityById(entityId);
        if (entity == null) return;
        FollowingRepeatableSoundInstance soundInstance = new FollowingRepeatableSoundInstance(entity, data);
        trackedSoundInstances.add(soundInstance);
        client.getSoundManager().play(soundInstance);
    }

    public static void handleDynamicSoundInstancePackets(MinecraftClient client, Identifier identifier, int entityId) {
        if (client.world == null) return;
        SoundInstanceHelper soundInstanceHelper = SoundInstanceHelper.fromIdentifier(identifier);
        Entity entity = client.world.getEntityById(entityId);
        if (soundInstanceHelper == null || entity == null) return;

        SoundInstance soundInstance;
        switch (soundInstanceHelper) {
            case OMINOUS_HEART -> {
                if (!(entity instanceof PlayerEntity playerEntity)) return;
                soundInstance = new OminousHeartSoundInstance(playerEntity);
            }
            case WHISPERS -> {
                if (!(entity instanceof PlayerEntity playerEntity)) return;
                soundInstance = new WhisperingSoundInstance(playerEntity);
            }
            case DROP_POT -> {
                if (!(entity instanceof DropPotEntity dropPotEntity)) return;
                soundInstance = new DropPotFlyingSoundInstance(dropPotEntity);
            }
            default -> {
                LoggerUtil.devLogger("Handling of SoundInstance packet has failed.");
                return;
            }
        }
        //FIXME: allow multiple Drop Pot sounds at the same time
        if (NeMuelchClient.SOUND_INSTANCE_CACHE.containsKey(soundInstance.getId())) {
            if (NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()) instanceof WhisperingSoundInstance whisperingSoundInstance) {
                whisperingSoundInstance.shouldFinish(true);
            } else if (NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()) != null) {
                // client.getSoundManager().stop(NeMuelchClient.SOUND_INSTANCE_CACHE.get(soundInstance.getId()));
            }
            // NeMuelchClient.SOUND_INSTANCE_CACHE.remove(soundInstance.getId());
        }
        NeMuelchClient.SOUND_INSTANCE_CACHE.put(soundInstance.getId(), soundInstance);
        client.getSoundManager().play(soundInstance);
    }
}
