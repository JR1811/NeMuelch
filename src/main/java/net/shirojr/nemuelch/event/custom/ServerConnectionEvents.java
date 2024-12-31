package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.network.NeMuelchS2CPacketHandler;

@SuppressWarnings("unused")
public class ServerConnectionEvents {
    @SuppressWarnings("Convert2MethodRef")
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerConnectionEvents.handleObfuscation(handler, sender, server);
            //ServerConnectionEvents.handleIllusions(handler, sender, server);
        });
    }

    private static void handleObfuscation(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        PacketByteBuf bufUpdateOthers = PacketByteBufs.create();
        bufUpdateOthers.writeUuid(handler.player.getUuid());
        bufUpdateOthers.writeBoolean(handler.player.hasStatusEffect(NeMuelchEffects.OBFUSCATED));

        for (ServerPlayerEntity entry : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(entry, NeMuelchS2CPacketHandler.UPDATE_OBFUSCATED_CACHE, bufUpdateOthers);
        }

        PacketByteBuf bufInitSelf = PacketByteBufs.create();
        bufInitSelf.writeVarInt(PlayerLookup.all(server).size());
        for (ServerPlayerEntity entry : PlayerLookup.all(server)) {
            bufInitSelf.writeUuid(entry.getUuid());
            bufInitSelf.writeBoolean(entry.hasStatusEffect(NeMuelchEffects.OBFUSCATED));
        }
        ServerPlayNetworking.send(handler.player, NeMuelchS2CPacketHandler.INIT_OBFUSCATED_CACHE, bufInitSelf);
    }
}
