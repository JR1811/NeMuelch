package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.NeMuelchTags;
import net.shirojr.nemuelch.util.RangeMapper;
import org.jetbrains.annotations.Nullable;

public class NeMuelchC2SPacketHandler {
    public static final Identifier KOCKING_RANGED_SOUND_CHANNEL = new Identifier(NeMuelch.MOD_ID, "knocking_ranged");
    public static final Identifier KOCKING_RAYCASTED_SOUND_CHANNEL = new Identifier(NeMuelch.MOD_ID, "knocking_raycasted");

    private static void handleKnockingSoundBroadcastPacket(boolean isRayCasted, MinecraftServer server, ServerPlayerEntity player,
                                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                           PacketSender sender) {
        BlockPos raycastBlockPos;
        if (isRayCasted) {
            raycastBlockPos = buf.readBlockPos();
            if (!player.getWorld().getBlockState(raycastBlockPos).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                raycastBlockPos = null;
            }
        } else raycastBlockPos = null;

        BlockPos packetBlockPos = raycastBlockPos;

        server.execute(() -> {
            if (!ConfigInit.CONFIG.allowKnocking) player.sendMessage(new TranslatableText("chat.nemuelch.feature_not_enabled"), false);

            ServerWorld world = player.getWorld();
            BlockPos hitBlockPos = getValidBlockPosInRange(packetBlockPos, world, player);

            if (hitBlockPos == null) player.sendMessage(new TranslatableText("chat.nemuelch.out_of_range"), true);
            else {
                double minPitch = 0.85, maxPitch = 1.2;

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
                    double distanceToBlock = player.getPos().distanceTo(new Vec3d(
                            hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5));

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

    /**
     * Always returns the raycasted blockpos, if the Block matched with the {@link ConfigInit#CONFIG}
     * @param packetBlockPos BLockPos which have been sent over the custom C2S Networking
     * @param world
     * @param player
     * @return <b><i>null</i></b>, if no suitable block was in range
     */
    @Nullable
    private static BlockPos getValidBlockPosInRange(BlockPos packetBlockPos, World world, PlayerEntity player) {
        BlockPos hitBlockPos = null;
        if (packetBlockPos != null) {
            return packetBlockPos;
        } else {
            Iterable<BlockPos> blockIterable = BlockPos.iterateOutwards(player.getBlockPos(),
                    ConfigInit.CONFIG.knockableBlockRange, ConfigInit.CONFIG.knockableBlockRange, ConfigInit.CONFIG.knockableBlockRange);

            for (BlockPos entry : blockIterable) {
                if (world.getBlockState(entry).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                    hitBlockPos = entry;
                    break;
                }
            }
        }

        return hitBlockPos;
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
