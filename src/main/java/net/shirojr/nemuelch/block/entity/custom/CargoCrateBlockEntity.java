package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;

public class CargoCrateBlockEntity extends BlockEntity {
    public CargoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CARGO_CRATE, pos, state);
    }
}
