package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockStateCallbacks {
    public static Event<BlockStateCallbacks.StateChanged> STATE_CHANGED = EventFactory.createArrayBacked(BlockStateCallbacks.StateChanged.class,
            listeners -> (world, pos, oldState, newState) -> {
                for (BlockStateCallbacks.StateChanged listener : listeners) {
                    listener.onBlockStateChanged(world, pos, oldState, newState);
                }
            }
    );

    public static Event<ModifyBlockPlacementState> MODIFY_STATE_FOR_PLACEMENT = EventFactory.createArrayBacked(ModifyBlockPlacementState.class,
            listeners -> (originalState, context) -> {
                for (ModifyBlockPlacementState listener : listeners) {
                    originalState = listener.onPlacementState(originalState, context);
                }
                return originalState;
            }
    );

    @FunctionalInterface
    public interface StateChanged {
        void onBlockStateChanged(World world, BlockPos pos, BlockState oldState, BlockState newState);
    }

    @FunctionalInterface
    public interface ModifyBlockPlacementState {
        @Nullable
        BlockState onPlacementState(@Nullable BlockState previousState, ItemPlacementContext context);
    }
}
