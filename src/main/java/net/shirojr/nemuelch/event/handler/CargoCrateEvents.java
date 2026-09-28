package net.shirojr.nemuelch.event.handler;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CargoCrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.event.custom.BlockCallbacks;
import net.shirojr.nemuelch.event.custom.BlockStateCallbacks;

public class CargoCrateEvents implements BlockCallbacks.BlockAdded, BlockStateCallbacks.StateChanged {
    @Override
    public void onBlockAdded(World world, BlockPos pos, BlockState state, BlockState oldState) {
        if (!(world.getBlockEntity(pos) instanceof Inventory)) return;
        BlockPos cargoCratePos = null;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (!(world.getBlockState(neighborPos).getBlock() instanceof CargoCrateBlock)) continue;
            cargoCratePos = neighborPos;
            break;
        }
        if (cargoCratePos == null) return;
        CachedBlockPosition corePos = CargoCrateBlock.getCore(world, cargoCratePos);
        if (corePos == null) return;
        if (!(corePos.getBlockEntity() instanceof CargoCrateBlockEntity blockEntity)) return;
        blockEntity.addConnectedNeighbor(cargoCratePos, pos);
    }

    @Override
    public void beforeBlockStateChanged(World world, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(world.getBlockEntity(pos) instanceof Inventory) || oldState.equals(newState)) return;
        BlockPos cargoCratePos = null;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);
            if (!(world.getBlockState(neighborPos).getBlock() instanceof CargoCrateBlock)) continue;
            cargoCratePos = neighborPos;
            break;
        }
        if (cargoCratePos == null) return;
        CachedBlockPosition corePos = CargoCrateBlock.getCore(world, cargoCratePos);
        if (corePos == null) return;
        if (!(corePos.getBlockEntity() instanceof CargoCrateBlockEntity blockEntity)) return;
        blockEntity.removeConnectedNeighbor(pos);
    }
}
