package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.shirojr.nemuelch.client.NeMuelchClientCache;
import net.shirojr.nemuelch.sound.SoundInstanceHandler;

public class ClientPlayerLeaveEvents implements ClientPlayConnectionEvents.Disconnect {
    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        NeMuelchClientCache.CAMERA_SHAKE_HANDLER.stopDisplacement();
        SoundInstanceHandler.handleStopSoundInstancePacket(client, null);
    }
}
