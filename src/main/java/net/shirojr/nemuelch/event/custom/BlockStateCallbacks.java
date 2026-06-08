package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStateCallbacks {
    public static Event<BlockStateCallbacks.StateChanged> STATE_CHANGED = EventFactory.createArrayBacked(BlockStateCallbacks.StateChanged.class,
            listeners -> (world, pos, oldState, newState) -> {
                for (BlockStateCallbacks.StateChanged listener : listeners) {
                    listener.onBlockStateChanged(world, pos, oldState, newState);
                }
            }
    );

    @FunctionalInterface
    public interface StateChanged {
        void onBlockStateChanged(World world, BlockPos pos, BlockState oldState, BlockState newState);
    }
}
