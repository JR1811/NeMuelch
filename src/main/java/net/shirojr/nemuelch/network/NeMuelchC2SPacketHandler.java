package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.DoorBlock;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.NeMuelchTags;
import net.shirojr.nemuelch.util.RangeMapper;

public class NeMuelchC2SPacketHandler {
    public static final Identifier KOCKING_SOUND_CHANNEL = new Identifier(NeMuelch.MOD_ID, "knocking");

    private static void handleKnockingSoundBroadcastPacket(MinecraftServer server, ServerPlayerEntity player,
                                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                           PacketSender sender) {
        server.execute(() -> {
            ServerWorld world = player.getWorld();

            BlockPos hitBlockPos = null;
            Iterable<BlockPos> blockIterable = BlockPos.iterateOutwards(player.getBlockPos(),
                    ConfigInit.CONFIG.hittableDoorRange, ConfigInit.CONFIG.hittableDoorRange, ConfigInit.CONFIG.hittableDoorRange);

            for (BlockPos blockPos : blockIterable) {
                if (world.getBlockState(blockPos).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                    hitBlockPos = blockPos;
                    break;
                }
            }

            if (hitBlockPos == null) {
                player.sendMessage(new TranslatableText("chat.nemuelch.out_of_range"), true);
            } else {
                double distanceToBlock = player.getPos().distanceTo(new Vec3d(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ()));

                double minVolume = 0.2, maxVolume = 1.0;
                double minPitch = 0.5, maxPitch = 1.5;

                RangeMapper possibleVolumeRange = new RangeMapper(0, ConfigInit.CONFIG.hittableDoorRange, minVolume, maxVolume);
                RangeMapper possiblePitchRange = new RangeMapper(0, ConfigInit.CONFIG.hittableDoorRange, minPitch, maxPitch);

                world.playSound(null, hitBlockPos, NeMuelchSounds.KNOCKING_01, SoundCategory.BLOCKS,
                        possibleVolumeRange.getRemappedFloatValue(distanceToBlock),
                        possiblePitchRange.getRemappedFloatValue(distanceToBlock));

                NeMuelch.devLogger("Playing sound for Block: " + world.getBlockState(hitBlockPos));
            }

            NeMuelch.devLogger("C2S network packet received");
        });
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(KOCKING_SOUND_CHANNEL, NeMuelchC2SPacketHandler::handleKnockingSoundBroadcastPacket);
    }
}
