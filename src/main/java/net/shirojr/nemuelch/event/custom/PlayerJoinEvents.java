package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import net.shirojr.nemuelch.init.NemuelchGameRules;

import java.util.UUID;

public class PlayerJoinEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(PlayerJoinEvents::onJoin);
    }

    private static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (!server.getGameRules().getBoolean(NemuelchGameRules.RESPAWN_LOCATIONS_CONFIG_FALLBACK)) return;
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(server.getScoreboard());
        UUID uuid = handler.player.getUuid();
        for (RespawnLocation assignedLocation : respawnComponent.getAssigned(uuid)) {
            if (assignedLocation.equals(RespawnLocation.DEFAULT)) return;
        }
        respawnComponent.assign(RespawnLocation.DEFAULT, uuid);
    }
}
