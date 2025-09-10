package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.datapack.RandomTickSpeedChanceDatapack;

public class ServerMiscEvents implements ServerLifecycleEvents.ServerStopped {
    @Override
    public void onServerStopped(MinecraftServer server) {
        RandomTickSpeedChanceDatapack.BLOCK_CHANCES.clear();
        NeMuelch.LOGGER.info("Stopped server instance and cleared Random Tick Chances datapack cache");
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerMiscEvents());
    }
}
