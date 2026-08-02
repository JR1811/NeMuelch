package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.shirojr.nemuelch.compat.cca.implementation.LocationalFadeComponent;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.compat.satin.shaders.FadeShader;
import net.shirojr.nemuelch.compat.satin.util.TransitioningCustomShader;

public class ClientPlayerTickingEvents implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) return;
        LocationalFadeComponent locationalFadeComponent = LocationalFadeComponent.get(world);
        double fade = locationalFadeComponent.getCombinedFade(player);
        FadeShader fadeShader = NeMuelchShaderManager.FADE.getInstance();
        if (!fadeShader.isRendered() && fade <= TransitioningCustomShader.THRESHOLD) return;
        fadeShader.setInstantZoneState((float) fade);
    }
}
