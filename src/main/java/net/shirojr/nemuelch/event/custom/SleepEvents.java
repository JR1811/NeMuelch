package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.network.NeMuelchS2CPacketHandler;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;

import java.util.ArrayList;

import static net.minecraft.state.property.Properties.ROTATION;

public class SleepEvents {
    public static void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeFloat(10);
                buf.writeUuid(player.getUuid());
                buf.writeBlockPos(sleepingPos);
                ServerPlayNetworking.send(player, NeMuelchS2CPacketHandler.SLEEP_EVENT_S2C_CHANNEL, buf);
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (entity instanceof ServerPlayerEntity player) {
                PacketByteBuf buf = PacketByteBufs.create();
                ServerPlayNetworking.send(player, NeMuelchS2CPacketHandler.CANCEL_SLEEP_EVENT_S2C_CHANNEL, buf);
            }
        });
    }

    public static void handleSpecialSleepEvent(Entity entity, BlockPos sleepingBlockPos) {
        NeMuelch.devLogger(entity + " went to bed");
        if (!(entity instanceof PlayerEntity player)) return;

        if (!SleepEventHelper.isSleepEventTime()) return;

        ServerWorld world = (ServerWorld) entity.world;
        int validMaxPosRange = 6, innerDeadZone = 2;
        Iterable<BlockPos> blockPosIterable = BlockPos.iterateOutwards(sleepingBlockPos, validMaxPosRange, validMaxPosRange, validMaxPosRange);

        BlockPos validBlockPos = null;
        for (BlockPos entry : blockPosIterable) {
            if (sleepingBlockPos.getY() >= entry.getY() + 3) {
                NeMuelch.devLogger("Valid pos was below bed");
                continue;
            }
            if (sleepingBlockPos.isWithinDistance(entry, innerDeadZone)) {
                NeMuelch.devLogger("Valid pos was too close!");
                continue;
            }

            if (world.getBlockState(entry).isSolidSurface(world, entry, player, Direction.UP) &&
                    world.getBlockState(entry.up()).getBlock().equals(Blocks.AIR)) {
                validBlockPos = entry.up();
                break;
            }

        }
        if (validBlockPos == null) {
            NeMuelch.devLogger("Couldn't find valid pos");
            return;
        }

        ArrayList<Text> lines = SleepEventHelper.getLines(sleepingBlockPos, world, player);
        if (lines == null) {
            NeMuelch.devLogger("No Text entries left in this world");
            return;
        }

        player.wakeUp();
        world.playSound(null, validBlockPos, NeMuelchSounds.EVENT_SLEEP_AMBIENT, SoundCategory.NEUTRAL, 0.7f, 0.75f);
        world.playSound(null, validBlockPos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.NEUTRAL, 0.7f, 1.0f);

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
}
