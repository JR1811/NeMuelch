package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.shirojr.nemuelch.NeMuelchClient;

public class ClientPlayerLeaveEvents implements ClientPlayConnectionEvents.Disconnect {
    @Override
    public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        NeMuelchClient.CAMERA_SHAKE_HANDLER.stopAndResetDisplacement();
    }
}
