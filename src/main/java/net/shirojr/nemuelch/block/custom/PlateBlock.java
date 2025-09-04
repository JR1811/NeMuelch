package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;

@SuppressWarnings("deprecation")
public class PlateBlock extends Block implements VariationHolder, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.FACING;

    private final Variation variant;

    public PlateBlock(Settings settings, Variation variant) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false).with(FACING, Direction.NORTH));
        this.variant = variant;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED, FACING);
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
    public Variation getVariant() {
        return variant;
    }

    @Override
    public Block getBlock() {
        return this;
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_plate");
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        return placementState
                .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER))
                .with(FACING, ctx.getPlayerLookDirection());
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
        int[][] elements = {
                {0, 0, 0, 16, 16, 2}
        };

        Direction facing = state.get(FACING);
        VoxelShape result = VoxelShapes.empty();
        for (int[] element : elements) {
            result = VoxelShapes.union(result, createRotatedShape(element, facing));
        }

        return result;
    }

    private VoxelShape createRotatedShape(int[] points, Direction direction) {
        return switch (direction) {
            case NORTH -> createCuboidShape(
                    points[0], points[1], points[2],
                    points[3], points[4], points[5]
            );
            case SOUTH -> createCuboidShape(
                    16 - points[3], points[1], 16 - points[5],
                    16 - points[0], points[4], 16 - points[2]
            );
            case WEST -> createCuboidShape(
                    points[2], points[1], 16 - points[3],
                    points[5], points[4], 16 - points[0]
            );
            case EAST -> createCuboidShape(
                    16 - points[5], points[1], points[0],
                    16 - points[2], points[4], points[3]
            );
            case DOWN -> createCuboidShape(
                    points[0], points[2], points[1],
                    points[3], points[5], points[4]
            ); // Y<->Z swap
            case UP -> createCuboidShape(points[0], 16 - points[5], 16 - points[4],
                    points[3], 16 - points[2], 16 - points[1]
            ); // Y<->Z swap + flip Y&Z
        };
    }
}
