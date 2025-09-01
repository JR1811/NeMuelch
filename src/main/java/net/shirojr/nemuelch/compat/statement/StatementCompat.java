package net.shirojr.nemuelch.compat.statement;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class StatementCompat {
    @SuppressWarnings("unused")
    public static void setPathStateIfPossible(ServerWorld world, BlockPos pos, boolean isPath) {
        BlockState state = world.getBlockState(pos);
        if (pathPropertyNotApplicable(state)) return;
        world.setBlockState(pos, state.with(StatementPropertyRegistry.IS_PATH, isPath));
    }

    public static BlockState getStateWithPath(BlockState state, boolean isPath) {
        if (pathPropertyNotApplicable(state)) return state;
        return state.with(StatementPropertyRegistry.IS_PATH, isPath);
    }

    public static boolean isNotPath(AbstractBlock.AbstractBlockState state) {
        if (pathPropertyNotApplicable(state)) return true;
        return !state.get(StatementPropertyRegistry.IS_PATH);
    }

    public static boolean pathPropertyNotApplicable(AbstractBlock.AbstractBlockState state) {
        if (isStatementMissing()) return true;
        return !state.contains(StatementPropertyRegistry.IS_PATH);
    }

    public static boolean isStatementMissing() {
        return !FabricLoader.getInstance().isModLoaded("statement");
    }

    public static void setToSand(@Nullable Entity entity, BlockState state, World world, BlockPos pos) {
        BlockState blockState = Block.pushEntitiesUpBeforeBlockChange(state, Blocks.SAND.getDefaultState(), world, pos);
        world.setBlockState(pos, blockState);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(entity, blockState));
    }

    public static void initialize() {
        if (isStatementMissing()) {
            return;
        }
        StatementPropertyRegistry.initialize();
    }
}
