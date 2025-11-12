package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;

import java.util.UUID;

public class PlayerJoinEvents implements ServerPlayConnectionEvents.Join {
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(server.getGameRules().getBoolean(NemuelchGameRules.THIRD_PERSON_ADMIN_ITEM_RENDERING_BLOCKING));
        ServerPlayNetworking.send(handler.player, NetworkIdentifiers.THIRD_PERSON_ITEM_RENDERING, buf);

        if (!server.getGameRules().getBoolean(NemuelchGameRules.RESPAWN_LOCATIONS_CONFIG_FALLBACK)) return;
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(server.getScoreboard());
        UUID uuid = handler.player.getUuid();
        for (RespawnLocation assignedLocation : respawnComponent.getAssigned(uuid)) {
            if (assignedLocation.equals(RespawnLocation.DEFAULT)) return;
        }
        respawnComponent.assign(RespawnLocation.DEFAULT, uuid);
    }
}
