package net.shirojr.nemuelch.event.handler;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.station.CargoCrateBlock;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.event.custom.BlockCallbacks;
import net.shirojr.nemuelch.event.custom.BlockStateCallbacks;

import java.util.ArrayList;
import java.util.List;

public class BlockEvents implements BlockStateCallbacks.StateChanged, BlockCallbacks.BlockAdded, BlockCallbacks.BlockPlaced {
    @Override
    public void onBlockStateChanged(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (oldState.isOf(newState.getBlock()) || oldState.isAir()) return;

        RopesComponent component = RopesComponent.get(serverWorld);
        long chunkKey = RopesComponent.getChunkKey(pos.getX(), pos.getZ());
        List<RopeData> unstableRopes = component.getUnstableRopesInChunk(chunkKey);
        if (!unstableRopes.isEmpty()) {
            List<RopeData> toRemove = new ArrayList<>();
            for (RopeData rope : unstableRopes) {
                if (!rope.contains(pos)) continue;
                toRemove.add(rope);
            }
            if (!toRemove.isEmpty()) {
                component.modifyRopes(true, ropeData -> ropeData.removeAll(toRemove));
            }
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, BlockState state, BlockState oldState) {

    }

    @Override
    public void onBlockPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack placedWith) {
        if (CargoCrateBlock.isValidCore(state)) {
            CargoCrateBlock.attemptConversion(world, pos, placer, placedWith);
            return;
        }
    }
}
