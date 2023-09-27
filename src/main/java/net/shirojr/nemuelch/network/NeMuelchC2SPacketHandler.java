package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
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
    public static final Identifier KOCKING_RANGED_SOUND_CHANNEL = new Identifier(NeMuelch.MOD_ID, "knocking_ranged");
    public static final Identifier KOCKING_RAYCASTED_SOUND_CHANNEL = new Identifier(NeMuelch.MOD_ID, "knocking_raycasted");

    private static void handleKnockingSoundBroadcastPacket(boolean isRayCasted, MinecraftServer server, ServerPlayerEntity player,
                                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                           PacketSender sender) {
        BlockPos tempBlockPos;
        if (isRayCasted) {
            tempBlockPos = buf.readBlockPos();
            if (!player.getWorld().getBlockState(tempBlockPos).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                tempBlockPos = null;
            }
        }
        else tempBlockPos = null;

        BlockPos packetBlockPos = tempBlockPos;

        server.execute(() -> {
            if (!ConfigInit.CONFIG.allowKnocking) {
                player.sendMessage(new TranslatableText("chat.nemuelch.feature_not_enabled"), false);
            }
            ServerWorld world = player.getWorld();

            BlockPos hitBlockPos = null;
            if (packetBlockPos == null) {
                Iterable<BlockPos> blockIterable = BlockPos.iterateOutwards(player.getBlockPos(),
                        ConfigInit.CONFIG.knockableBlockRange, ConfigInit.CONFIG.knockableBlockRange, ConfigInit.CONFIG.knockableBlockRange);

                for (BlockPos entry : blockIterable) {
                    if (world.getBlockState(entry).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                        hitBlockPos = entry;
                        break;
                    }
                }
            } else {
                hitBlockPos = packetBlockPos;
            }

            if (hitBlockPos == null) {
                player.sendMessage(new TranslatableText("chat.nemuelch.out_of_range"), true);
            } else {
                double minPitch = 0.85, maxPitch = 1.2;
                double distanceToBlock = player.getPos().distanceTo(new Vec3d(
                        hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5));

                float remappedPitchValue;
                RangeMapper possiblePitchRange;

                if (world.getBlockEntity(hitBlockPos) instanceof Inventory inventory) {
                    int occupiedSlots = 0;
                    for (int i = 0; i < inventory.size(); i++) {
                        if (inventory.getStack(i).getItem().equals(Items.AIR)) occupiedSlots++;
                    }
                    possiblePitchRange = new RangeMapper(0, inventory.size(), minPitch, maxPitch);
                    remappedPitchValue = possiblePitchRange.getRemappedFloatValue(occupiedSlots);

                } else {
                    possiblePitchRange = new RangeMapper(0, ConfigInit.CONFIG.knockableBlockRange, minPitch, maxPitch);
                    remappedPitchValue = possiblePitchRange.getRemappedFloatValue(distanceToBlock);
                }

                world.playSound(null, hitBlockPos, NeMuelchSounds.KNOCKING_01, SoundCategory.BLOCKS,
                        ConfigInit.CONFIG.knockingVolume, remappedPitchValue);

                NeMuelch.devLogger("Playing sound for Block: " + world.getBlockState(hitBlockPos) + " at: " + hitBlockPos);
            }

            NeMuelch.devLogger("Pressed Keybind and sent C2S network packet");
        });
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(KOCKING_RANGED_SOUND_CHANNEL, (server, player, handler, buf, responseSender) -> {
            NeMuelchC2SPacketHandler.handleKnockingSoundBroadcastPacket(false, server, player, handler, buf, responseSender);
        });
        ServerPlayNetworking.registerGlobalReceiver(KOCKING_RAYCASTED_SOUND_CHANNEL, (server, player, handler, buf, responseSender) -> {
            NeMuelchC2SPacketHandler.handleKnockingSoundBroadcastPacket(true, server, player, handler, buf, responseSender);
        });
    }
}
