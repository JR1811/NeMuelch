package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;

public class BlockEntityEvents implements ServerBlockEntityEvents.Load, ServerBlockEntityEvents.Unload {
    @Override
    public void onLoad(BlockEntity blockEntity, ServerWorld world) {
        if (blockEntity instanceof CrateBlockEntity crateBlockEntity) {

        }
    }

    @Override
    public void onUnload(BlockEntity blockEntity, ServerWorld world) {

    }
}
