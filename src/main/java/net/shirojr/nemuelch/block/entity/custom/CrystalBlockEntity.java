package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

public class CrystalBlockEntity extends BlockEntity {
    private int innerColor;
    private int outerColor;

    public CrystalBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CRYSTAL, pos, state);
    }

    public int getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(int innerColor) {
        boolean changed = this.innerColor != innerColor;
        this.innerColor = innerColor;
        if (changed) markDirty();
    }

    public int getOuterColor() {
        return outerColor;
    }

    public void setOuterColor(int outerColor) {
        boolean changed = this.outerColor != outerColor;
        this.outerColor = outerColor;
        if (changed) markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY)) {
            this.innerColor = nbt.getInt(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY);
        }
        if (nbt.contains(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY)) {
            this.outerColor = nbt.getInt(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY);
        }
        if (world != null && world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt(NeMuelchNbtKeys.INNER_COLOR_NBT_KEY, this.innerColor);
        nbt.putInt(NeMuelchNbtKeys.OUTER_COLOR_NBT_KEY, this.outerColor);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(getPos());
        }
    }
}
