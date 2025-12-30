package net.shirojr.nemuelch.util.duck;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public interface IrisConfigShaderToggleLock {
    boolean neMuelch$isLocked();

    void neMuelch$setLocked(boolean locked);

    default void onLockedInteraction(MinecraftClient client) {
        if (client == null) return;
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.8f));
        if (client.player == null) return;
        client.player.sendMessage(Text.literal("Blocked Interaction"));
    }
}
