package net.shirojr.nemuelch.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;
import net.shirojr.nemuelch.sound.instance.OminousHeartSoundInstance;
import net.shirojr.nemuelch.sound.instance.TntStickItemEntitySoundInstance;
import net.shirojr.nemuelch.sound.instance.WhisperingSoundInstance;
import net.shirojr.nemuelch.util.helper.SoundInstanceHelper;

public class SoundInstanceHandler {

    public static void handleSoundInstancePackets(MinecraftClient client, Identifier identifier, int entityId) {
        if (client.world == null) return;
        SoundInstanceHelper soundInstanceHelper = SoundInstanceHelper.fromIdentifier(identifier);
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
    }
}
