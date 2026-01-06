package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.world.ClientWorld;

public class ClientTickingEvents implements ClientTickEvents.EndWorldTick {
    @Override
    public void onEndTick(ClientWorld world) {

    }
}
