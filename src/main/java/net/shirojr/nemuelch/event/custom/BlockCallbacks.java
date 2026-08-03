package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCallbacks {
    public static Event<BlockAdded> ON_ADDED = EventFactory.createArrayBacked(BlockAdded.class,
            listeners -> (world, pos, state, oldState) -> {
                for (BlockAdded listener : listeners) {
                    listener.onBlockAdded(world, pos, state, oldState);
                }
            }
    );

    public static Event<BlockPlaced> ON_PLACED = EventFactory.createArrayBacked(BlockPlaced.class,
            listeners -> (world, pos, state, placer, placedStack) -> {
                for (BlockPlaced listener : listeners) {
                    listener.onBlockPlaced(world, pos, state, placer, placedStack);
                }
            }
    );

    @FunctionalInterface
    public interface BlockAdded {
        void onBlockAdded(World world, BlockPos pos, BlockState state, BlockState oldState);
    }

    @FunctionalInterface
    public interface BlockPlaced {
        void onBlockPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack placedWith);
    }
}
