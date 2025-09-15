package net.shirojr.nemuelch.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.monster.GeneralMonsterComponent;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
public class NeMuelchC2SNetworking {
    static {
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.KNOCKING_RAYCASTED_SOUND_C2S, NeMuelchC2SNetworking::handleKnockingSoundBroadcastPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.MOUSE_SCROLLED_C2S, NeMuelchC2SNetworking::handleMouseScrolledPacket);
        ServerPlayNetworking.registerGlobalReceiver(NetworkIdentifiers.MONSTER_ABILITY_KEY, NeMuelchC2SNetworking::handleMonsterAbilityKey);
    }

    private static void handleMonsterAbilityKey(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int key = buf.readVarInt();
        server.execute(() -> {
            GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
            for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
                entry.getAbilities().onKeybindPressed(player, key);
            }
        });
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
                    if (inventory.getStack(i).getItem().equals(Items.AIR)) occupiedSlots++;
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
