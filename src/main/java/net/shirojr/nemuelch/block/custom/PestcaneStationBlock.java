package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PestcaneStationBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public PestcaneStationBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case NORTH:
                return SHAPE_N;
            case EAST:
                return SHAPE_E;
            case SOUTH:
                return SHAPE_S;
            case WEST:
                return SHAPE_W;
            default:
                return SHAPE_N;
        }
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {

        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {

        builder.add(FACING);
    }


    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(3, 15, 4, 13, 16, 12),
            Block.createCuboidShape(3, 0, 3, 13, 15, 13),
            Block.createCuboidShape(0, 0, 0, 3, 1, 3),
            Block.createCuboidShape(1, 1, 1, 2, 12, 2),
            Block.createCuboidShape(14, 1, 14, 15, 12, 15),
            Block.createCuboidShape(13, 0, 13, 16, 1, 16),
            Block.createCuboidShape(1, 12, 1, 2, 14, 12),
            Block.createCuboidShape(14, 12, 4, 15, 14, 15),
            Block.createCuboidShape(2, 0, 4, 3, 15, 12),
            Block.createCuboidShape(13, 0, 4, 14, 15, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(4, 15, 3, 12, 16, 13),
            Block.createCuboidShape(3, 0, 3, 13, 15, 13),
            Block.createCuboidShape(13, 0, 0, 16, 1, 3),
            Block.createCuboidShape(14, 1, 1, 15, 12, 2),
            Block.createCuboidShape(1, 1, 14, 2, 12, 15),
            Block.createCuboidShape(0, 0, 13, 3, 1, 16),
            Block.createCuboidShape(4, 12, 1, 15, 14, 2),
            Block.createCuboidShape(1, 12, 14, 12, 14, 15),
            Block.createCuboidShape(4, 0, 2, 12, 15, 3),
            Block.createCuboidShape(4, 0, 13, 12, 15, 14)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(3, 15, 4, 13, 16, 12),
            Block.createCuboidShape(3, 0, 3, 13, 15, 13),
            Block.createCuboidShape(13, 0, 13, 16, 1, 16),
            Block.createCuboidShape(14, 1, 14, 15, 12, 15),
            Block.createCuboidShape(1, 1, 1, 2, 12, 2),
            Block.createCuboidShape(0, 0, 0, 3, 1, 3),
            Block.createCuboidShape(14, 12, 4, 15, 14, 15),
            Block.createCuboidShape(1, 12, 1, 2, 14, 12),
            Block.createCuboidShape(13, 0, 4, 14, 15, 12),
            Block.createCuboidShape(2, 0, 4, 3, 15, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(4, 15, 3, 12, 16, 13),
            Block.createCuboidShape(3, 0, 3, 13, 15, 13),
            Block.createCuboidShape(0, 0, 13, 3, 1, 16),
            Block.createCuboidShape(1, 1, 14, 2, 12, 15),
            Block.createCuboidShape(14, 1, 1, 15, 12, 2),
            Block.createCuboidShape(13, 0, 0, 16, 1, 3),
            Block.createCuboidShape(1, 12, 14, 12, 14, 15),
            Block.createCuboidShape(4, 12, 1, 15, 14, 2),
            Block.createCuboidShape(4, 0, 13, 12, 15, 14),
            Block.createCuboidShape(4, 0, 2, 12, 15, 3)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
}
