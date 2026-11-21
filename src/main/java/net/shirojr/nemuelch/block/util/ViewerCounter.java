package net.shirojr.nemuelch.block.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.screen.util.InventoryHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ViewerCounter extends ViewerCountManager {
    private final BlockEntity blockEntity;
    private final Supplier<Inventory> inventorySupplier;
    @Nullable
    private final SoundData openSound;
    @Nullable
    private final SoundData closeSound;

    public ViewerCounter(BlockEntity blockEntity, Supplier<Inventory> inventorySupplier,
                         @Nullable SoundData openSound, @Nullable SoundData closeSound) {
        this.blockEntity = blockEntity;
        this.inventorySupplier = inventorySupplier;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (this.openSound != null) serverWorld.playSound(null, pos, this.openSound.sound, SoundCategory.BLOCKS,
                this.openSound.volume, this.openSound.pitch);
    }

    @Override
    protected void onContainerClose(World world, BlockPos pos, BlockState state) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (this.closeSound != null) serverWorld.playSound(null, pos, this.closeSound.sound, SoundCategory.BLOCKS,
                this.closeSound.volume, this.closeSound.pitch);
    }

    @Override
    protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
        // NO-OP
    }

    @Override
    protected boolean isPlayerViewing(PlayerEntity player) {
        ScreenHandler currentScreenHandler = player.currentScreenHandler;
        if (currentScreenHandler instanceof InventoryHolder<?> inventoryHolder) {
            return this.inventorySupplier.get() == inventoryHolder.getInventory();
        } else {
            return false;
        }
    }

    public record SoundData(SoundEvent sound, float volume, float pitch) {
    }
}
