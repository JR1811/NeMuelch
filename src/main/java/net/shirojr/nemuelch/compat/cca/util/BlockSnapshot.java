package net.shirojr.nemuelch.compat.cca.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public record BlockSnapshot(BlockPos pos, BlockState state) {
}
