package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.adminToolItem.RopeToolItem;
import net.shirojr.nemuelch.misc.EntitySlowingFeature;
import net.shirojr.nemuelch.network.packet.MonsterAbilityKeyPressC2SPacket;
import net.shirojr.nemuelch.network.packet.RopeDeletionC2SPacket;
import net.shirojr.nemuelch.network.packet.RopeModificationC2SPacket;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class NeMuelchC2SNetworking {
    static {
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.KNOCKING_RAYCASTED_SOUND_C2S, NeMuelchC2SNetworking::handleKnockingSoundBroadcastPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.MOUSE_SCROLLED_POT_LAUNCHER_C2S, NeMuelchC2SNetworking::handleMouseScrolledPotLauncherPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.MOUSE_SCROLLED_SLOWING_C2S, NeMuelchC2SNetworking::handleMouseScrolledSlowingPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ADVANCED_FOG_SCREEN_DATA_CHANGE, NeMuelchC2SNetworking::handleAdvancedFogScreenData);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.ADVANCED_FOG_REQUEST_SELF_SYNC, NeMuelchC2SNetworking::handleAdvancedFogRequestSelfSync);

        ServerPlayNetworking.registerGlobalReceiver(RopeModificationC2SPacket.TYPE, NeMuelchC2SNetworking::handleRopeModification);
        ServerPlayNetworking.registerGlobalReceiver(RopeDeletionC2SPacket.TYPE, NeMuelchC2SNetworking::handleRopeDeletion);
        ServerPlayNetworking.registerGlobalReceiver(MonsterAbilityKeyPressC2SPacket.TYPE, NeMuelchC2SNetworking::handleMonsterAbilityKey);
    }

    private static void handleRopeDeletion(RopeDeletionC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        player.server.execute(() -> {
            RopesComponent component = RopesComponent.get(player.getServerWorld());
            RopeData rope = component.getRope(packet.ropePosA(), packet.ropePosB());
            ItemStack stack = player.getMainHandStack();
            if (rope == null || RopeToolItem.isSettingsUsageBlocked(player, stack, rope, 50)) return;
            component.modifyRopes(true, ropes -> ropes.remove(ropes.indexOf(rope)));
        });
    }

    private static void handleRopeModification(RopeModificationC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        player.server.execute(() -> {
            RopesComponent component = RopesComponent.get(player.getServerWorld());
            RopeData rope = component.getRope(packet.ropePosA(), packet.ropePosB());
            ItemStack stack = player.getMainHandStack();
            if (rope == null || RopeToolItem.isSettingsUsageBlocked(player, stack, rope, 50)) return;
            if (!(stack.getItem() instanceof RopeToolItem ropeToolItem)) return;
            component.modifyRopes(true, ropes ->
                    ropes.set(ropes.indexOf(rope), new RopeData(rope.pointA(), rope.pointB(), packet.segments(), packet.width(), packet.slack(), packet.stable()))
            );
        });
    }

    private static void handleMouseScrolledSlowingPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        double delta = buf.readDouble();
        server.execute(() -> EntitySlowingFeature.handleScroll(player, delta));
    }

    private static void handleAdvancedFogRequestSelfSync(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos blockEntityPos = BlockPos.fromLong(buf.readLong());

        server.execute(() -> {
            if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
            if (!(serverWorld.getBlockEntity(blockEntityPos) instanceof AdvancedFogBlockEntity blockEntity)) return;
            PacketByteBuf requestedBuf = PacketByteBufs.create();
            requestedBuf.writeLong(blockEntityPos.asLong());
            blockEntity.getData().toPacketByteBuf(requestedBuf);
            ServerPlayNetworking.send(player, NetworkIdentifiers.ADVANCED_FOG_SYNC, requestedBuf);
        });
    }

    private static void handleAdvancedFogScreenData(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos blockEntityPos = BlockPos.fromLong(buf.readLong());
        AdvancedFogBlockEntity.Data data = AdvancedFogBlockEntity.Data.fromPacketByteBuf(buf);

        server.execute(() -> {
            double maxReachDistance = 5;
            if (!player.isCreative()) return;
            if (player.getPos().distanceTo(blockEntityPos.toCenterPos()) > maxReachDistance) return;
            if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
            BlockEntity retrievedBlockEntity = serverWorld.getBlockEntity(blockEntityPos);
            if (!(retrievedBlockEntity instanceof AdvancedFogBlockEntity blockEntity)) return;
            blockEntity.setData(data, true);
            player.sendMessage(Text.literal("Applied data"), true);
            server.sendMessage(Text.literal(player.getName().getString() + " changed values for Advanced Fog Block at: " + blockEntityPos.toShortString()));
        });
    }

    private static void handleMonsterAbilityKey(MonsterAbilityKeyPressC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        int key = packet.index();
        boolean pressed = packet.pressed();

        MinecraftServer server = player.getServer();
        if (server == null) return;
        server.execute(() -> MonsterComponent.get(player).getAbilities().pressedKey(key, pressed, player));
    }

    private static void handleMouseScrolledPotLauncherPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
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

    private static void handleKnockingSoundBroadcastPacket(MinecraftServer server, ServerPlayerEntity player,
                                                           ServerPlayNetworkHandler handler, PacketByteBuf buf,
                                                           PacketSender sender) {
        BlockPos hitPos = buf.readBlockPos();
        server.execute(() -> {
            if (!NeMuelchConfigInit.CONFIG.allowKnocking) {
                player.sendMessage(Text.translatable("chat.nemuelch.feature_not_enabled"), false);
                return;
            }

            ServerWorld world = player.getServerWorld();
            BlockState hitState = world.getBlockState(hitPos);
            if (!hitState.isIn(NeMuelchTags.Blocks.KNOCK_SOUND_BLOCKS)) return;

            float minPitch = 0.85f, maxPitch = 1.2f;

            float pitch;

            if (world.getBlockEntity(hitPos) instanceof Inventory inventory) {
                int occupiedSlots = 0;
                for (int i = 0; i < inventory.size(); i++) {
                    if (!inventory.getStack(i).isEmpty()) {
                        occupiedSlots++;
                    }
                }
                pitch = MathHelper.lerp((float) occupiedSlots / inventory.size(), minPitch, maxPitch);

            } else {
                double sqDistanceToBlock = player.squaredDistanceTo(hitPos.toCenterPos());
                float normalizedDistance = (float) MathHelper.clamp(
                        sqDistanceToBlock / (NeMuelchConfigInit.CONFIG.knockableBlockRange * NeMuelchConfigInit.CONFIG.knockableBlockRange),
                        0, 1
                );
                pitch = (float) MathHelper.lerp(Math.pow(normalizedDistance, 2), minPitch, maxPitch);
            }
            world.playSound(null, hitPos, NeMuelchSounds.KNOCKING_01, SoundCategory.BLOCKS,
                    NeMuelchConfigInit.CONFIG.knockingVolume, pitch);

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

    public static void initialize() {
        // static initialisation
    }
}
