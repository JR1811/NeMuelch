package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.network.NeMuelchS2CPacketHandler;

public class ServerConnectionEvents {
    @SuppressWarnings("CodeBlock2Expr")
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PacketByteBuf bufUpdateOthers = PacketByteBufs.create();
            bufUpdateOthers.writeUuid(handler.player.getUuid());
            bufUpdateOthers.writeBoolean(handler.player.hasStatusEffect(NeMuelchEffects.OBFUSCATED));

            PlayerLookup.all(server).forEach(player -> {
                ServerPlayNetworking.send(player, NeMuelchS2CPacketHandler.UPDATE_OBFUSCATED_CACHE, bufUpdateOthers);
            });

            PacketByteBuf bufInitSelf = PacketByteBufs.create();
            bufInitSelf.writeVarInt(PlayerLookup.all(server).size());
            for (ServerPlayerEntity entry : PlayerLookup.all(server)) {
                bufInitSelf.writeUuid(entry.getUuid());
                bufInitSelf.writeBoolean(entry.hasStatusEffect(NeMuelchEffects.OBFUSCATED));
            }
            ServerPlayNetworking.send(handler.player, NeMuelchS2CPacketHandler.INIT_OBFUSCATED_CACHE, bufInitSelf);
        });
    }
}
