package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;

import java.util.UUID;

@SuppressWarnings("unused")
public class ServerPlayerJoinLeaveEvents implements ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect {
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        syncThirdPersonItemRenderingGameRule(server, handler.player);
        syncBoatGameRules(server, handler.player);
        syncRespawnLocation(server, handler.player);
        syncPullUpVertStrength(server, handler.player);
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        distributeOccasionLeaving(server, handler.player);
    }

    private void distributeOccasionJoining(MinecraftServer server, ServerPlayerEntity target) {
        if (!(target.getWorld() instanceof ServerWorld world)) return;
        OccasionsWorldComponent.get(world).getUnsyncedActiveOccasions().forEach(entry -> entry.onPlayerJoinedWorldWhileActive(target));
    }

    private void distributeOccasionLeaving(MinecraftServer server, ServerPlayerEntity target) {
        if (!(target.getWorld() instanceof ServerWorld world)) return;
        OccasionsWorldComponent.get(world).getUnsyncedActiveOccasions().forEach(entry -> entry.onPlayerLeftWorldWhileActive(target));
    }

    private void syncThirdPersonItemRenderingGameRule(MinecraftServer server, ServerPlayerEntity target) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(server.getGameRules().getBoolean(NemuelchGameRules.THIRD_PERSON_ADMIN_ITEM_RENDERING_BLOCKING));
        ServerPlayNetworking.send(target, NetworkIdentifiers.THIRD_PERSON_ITEM_RENDERING, buf);
    }

    private void syncBoatGameRules(MinecraftServer server, ServerPlayerEntity target) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(server.getGameRules().getInt(NemuelchGameRules.BOAT_DEEP_WATER_ENDURANCE));
        ServerPlayNetworking.send(target, NetworkIdentifiers.DEEP_WATER_BOAT_ENDURANCE_SYNC, buf);
    }

    private void syncRespawnLocation(MinecraftServer server, ServerPlayerEntity target) {
        if (!server.getGameRules().getBoolean(NemuelchGameRules.RESPAWN_LOCATIONS_CONFIG_FALLBACK)) return;
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(server.getScoreboard());
        UUID uuid = target.getUuid();
        for (RespawnLocation assignedLocation : respawnComponent.getAssigned(uuid)) {
            if (assignedLocation.equals(RespawnLocation.DEFAULT)) return;
        }
        respawnComponent.assign(RespawnLocation.DEFAULT, uuid);
    }

    private void syncPullUpVertStrength(MinecraftServer server, ServerPlayerEntity target) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(server.getGameRules().get(NemuelchGameRules.PULL_UP_VERT_STRENGTH).get());
        ServerPlayNetworking.send(target, NetworkIdentifiers.PULL_UP_VERT_STRENGTH_GAMERULE_SYNC, buf);
    }
}
