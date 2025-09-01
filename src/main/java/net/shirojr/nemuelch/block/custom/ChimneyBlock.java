package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;

@SuppressWarnings("deprecation")
public class ChimneyBlock extends PillarBlock implements VariationHolder, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private final Variation variant;

    public ChimneyBlock(Settings settings, Variation variant) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
        this.variant = variant;
    }

    @Override
    public Variation getVariant() {
        return variant;
    }

    @Override
    public Block getBlock() {
        return this;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        return placementState.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int[][] walls = {
                {0, 0, 0, 14, 16, 2},    // wall1
                {14, 0, 0, 16, 16, 14},  // wall2
                {2, 0, 14, 16, 16, 16},  // wall3
                {0, 0, 2, 2, 16, 16}     // wall4
        };

        Direction.Axis axis = state.get(AXIS);
        VoxelShape result = VoxelShapes.empty();
        for (int[] wall : walls) {
            result = VoxelShapes.union(result, createRotatedShape(wall, axis));
        }

        return result;
    }

    private VoxelShape createRotatedShape(int[] points, Direction.Axis axis) {
        return switch (axis) {
            case X -> createCuboidShape(points[1], points[0], points[2], points[4], points[3], points[5]); // swap X<->Y
            case Z -> createCuboidShape(points[0], points[2], points[1], points[3], points[5], points[4]); // swap Y<->Z
            default ->
                    createCuboidShape(points[0], points[1], points[2], points[3], points[4], points[5]); // Y-axis (no swap)
        };
    }
}
