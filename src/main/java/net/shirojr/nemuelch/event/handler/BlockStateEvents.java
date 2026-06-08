package net.shirojr.nemuelch.event.handler;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.event.custom.BlockStateCallbacks;

import java.util.ArrayList;
import java.util.List;

public class BlockStateEvents implements BlockStateCallbacks.StateChanged {
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
}
