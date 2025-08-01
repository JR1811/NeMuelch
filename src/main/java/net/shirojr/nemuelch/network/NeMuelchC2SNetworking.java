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
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.event.custom.SleepEvents;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.RangeMapper;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class NeMuelchC2SNetworking {
    static {
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.KNOCKING_RANGED_SOUND_C2S, (server, player, handler, buf, responseSender) ->
                NeMuelchC2SNetworking.handleKnockingSoundBroadcastPacket(false, server, player, handler, buf, responseSender));
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.KNOCKING_RAYCASTED_SOUND_C2S, (server, player, handler, buf, responseSender) ->
                NeMuelchC2SNetworking.handleKnockingSoundBroadcastPacket(true, server, player, handler, buf, responseSender));
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.SLEEP_EVENT_C2S, NeMuelchC2SNetworking::handleSleepEventPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.MOUSE_SCROLLED_C2S, NeMuelchC2SNetworking::handleMouseScrolledPacket);
    }

    private static void handleMouseScrolledPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int id = buf.readVarInt();
        double delta = buf.readDouble();
        Optional<PotLauncherEntity.InteractionHitBox> selectedBox = PotLauncherEntity.InteractionHitBox.byName(buf.readString());

        server.execute(() -> {
            if (!(player.getWorld().getEntityById(id) instanceof PotLauncherEntity entity)) return;
            if (selectedBox.isEmpty()) return;
            if (!selectedBox.get().isScrollable()) return;
            selectedBox.get().onHit(entity, delta, 5f);
        });
    }

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
            if (!NeMuelchConfigInit.CONFIG.allowKnocking)
                player.sendMessage(Text.translatable("chat.nemuelch.feature_not_enabled"), false);

            ServerWorld world = player.getServerWorld();
            BlockPos hitBlockPos = getValidBlockPosInRange(packetBlockPos, world, player);

            if (hitBlockPos == null) player.sendMessage(Text.translatable("chat.nemuelch.out_of_range"), true);
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

                    possiblePitchRange = new RangeMapper(0, NeMuelchConfigInit.CONFIG.knockableBlockRange, minPitch, maxPitch);
                    remappedPitchValue = possiblePitchRange.getRemappedFloatValue(distanceToBlock);
                }

                world.playSound(null, hitBlockPos, NeMuelchSounds.KNOCKING_01, SoundCategory.BLOCKS,
                        NeMuelchConfigInit.CONFIG.knockingVolume, remappedPitchValue);

                LoggerUtil.devLogger("Playing sound for Block: " + world.getBlockState(hitBlockPos) + " at: " + hitBlockPos);
            }

            LoggerUtil.devLogger("Pressed Keybind and sent C2S network packet");
        });
    }

    @Nullable
    private static BlockPos getValidBlockPosInRange(BlockPos packetBlockPos, World world, PlayerEntity player) {
        BlockPos hitBlockPos = null;
        if (packetBlockPos != null) {
            return packetBlockPos;
        } else {
            Iterable<BlockPos> blockIterable = BlockPos.iterateOutwards(player.getBlockPos(),
                    NeMuelchConfigInit.CONFIG.knockableBlockRange,
                    NeMuelchConfigInit.CONFIG.knockableBlockRange,
                    NeMuelchConfigInit.CONFIG.knockableBlockRange);
            for (BlockPos entry : blockIterable) {
                if (world.getBlockState(entry).isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) {
                    hitBlockPos = entry;
                    break;
                }
            }
        }

        return hitBlockPos;
    }

    private static void handleSleepEventPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                               PacketByteBuf buf, PacketSender responseSender) {
        BlockPos sleepingPos = buf.readBlockPos();
        server.execute(() -> SleepEvents.handleSpecialSleepEvent(player, sleepingPos));
    }

    public static void initialize() {
        // static initialisation
    }
}
