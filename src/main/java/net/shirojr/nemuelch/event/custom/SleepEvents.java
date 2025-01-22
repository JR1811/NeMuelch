package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;
import net.shirojr.nemuelch.world.PersistentWorldData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static net.minecraft.state.property.Properties.ROTATION;

public class SleepEvents {
    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                float delay = entity.getWorld().getRandom().nextFloat(10, 60);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeFloat(delay);
                buf.writeBlockPos(sleepingPos);
                ServerPlayNetworking.send(player, NetworkIdentifiers.SLEEP_EVENT_S2C, buf);
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PacketByteBuf buf = PacketByteBufs.create();
                ServerPlayNetworking.send(player, NetworkIdentifiers.SLEEP_EVENT_S2C, buf);
            }
        });
    }

    public static void handleSpecialSleepEvent(Entity entity, BlockPos sleepingBlockPos) {
        LoggerUtil.devLogger(entity + " went to bed");
        ServerWorld world = (ServerWorld) entity.world;
        int chance = NeMuelchConfigInit.CONFIG.specialSleepEventChance;

        if (!SleepEventHelper.isSleepEventTime()) return;
        if (!(entity instanceof PlayerEntity player)) return;
        if (playerExecutedEventAlready(world, player)) return;
        if (chance > 0 && world.random.nextInt(0, chance) > 0) return;
        int validMaxPosRange = 10, innerDeadZone = 3;
        Iterable<BlockPos> blockPosIterable = BlockPos.iterateOutwards(sleepingBlockPos, validMaxPosRange, validMaxPosRange, validMaxPosRange);
        BlockPos validBlockPos = getValidBlockPosForSign(blockPosIterable, sleepingBlockPos, world, player, innerDeadZone);
        if (validBlockPos == null) {
            LoggerUtil.devLogger("Couldn't find valid pos");
            return;
        }

        ArrayList<Text> lines = SleepEventHelper.getAvailableLines(sleepingBlockPos, world, player);
        if (lines == null) {
            LoggerUtil.devLogger("No Text entries left in this world");
            return;
        }

        world.getServer().sendSystemMessage(new LiteralText("Special Sleep event executed by " + player.getName()), player.getUuid());

        player.wakeUp();
        world.playSound(null, validBlockPos, NeMuelchSounds.EVENT_SLEEP_AMBIENT, SoundCategory.NEUTRAL, 1.0f, 0.75f);
        world.playSound(null, validBlockPos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.NEUTRAL, 1.0f, 1.0f);

        double xComponent = sleepingBlockPos.getX() - validBlockPos.getX();
        double zComponent = sleepingBlockPos.getZ() - validBlockPos.getZ();
        float signBlockYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(zComponent, xComponent) * 57.2957763671875) - 90.0f);

        world.setBlockState(validBlockPos, Blocks.SPRUCE_SIGN.getDefaultState().with(ROTATION,
                MathHelper.floor((double) (signBlockYaw * 16.0f / 360.0f) + 0.5) & 0xF));

        if (world.getBlockEntity(validBlockPos) instanceof SignBlockEntity signBlockEntity) {
            for (int i = 0; i < 4; i++) {
                signBlockEntity.setTextOnRow(i, lines.get(i));
            }
        }
    }

    private static boolean playerExecutedEventAlready(ServerWorld world, PlayerEntity player) {
        MinecraftServer server = world.getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);
        for (var entry : persistentWorldData.usedSleepEventEntries) {
            if (entry.playerName().equals(player.getName().getString())) {
                LoggerUtil.devLogger("Player activated a Special Sleep Event already!");
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static BlockPos getValidBlockPosForSign(Iterable<BlockPos> blockPosIterable, BlockPos sleepingBlockPos,
                                                    ServerWorld world, PlayerEntity player, int innerDeadZone) {
        BlockPos validBlockPos = null;
        for (BlockPos entry : blockPosIterable) {
            if (sleepingBlockPos.getY() >= entry.getY() + 3) {
                LoggerUtil.devLogger("Valid pos was below bed");
                continue;
            }
            if (sleepingBlockPos.isWithinDistance(entry, innerDeadZone)) {
                LoggerUtil.devLogger("Valid pos was too close!");
                continue;
            }
            if (world.getBlockState(entry).isSolidSurface(world, entry, player, Direction.UP) &&
                    world.getBlockState(entry.up()).getBlock().equals(Blocks.AIR)) {
                validBlockPos = entry.up();
                break;
            }

        }
        return validBlockPos;
    }
}
