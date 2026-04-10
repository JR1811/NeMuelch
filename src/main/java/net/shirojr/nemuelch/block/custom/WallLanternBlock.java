package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class WallLanternBlock extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;

    public WallLanternBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(WATERLOGGED, false)
                .with(HORIZONTAL_FACING, Direction.NORTH)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED, HORIZONTAL_FACING);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.getAttachableDirections(world, pos).contains(state.get(HORIZONTAL_FACING).getOpposite());
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction requestedDirection = ctx.getSide();
        if (!getAttachableDirections(ctx.getWorld(), ctx.getBlockPos()).contains(requestedDirection.getOpposite())) {
            return null;
        }
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState()
                .with(HORIZONTAL_FACING, requestedDirection)
                .with(WATERLOGGED, fluidState.getFluid().equals(Fluids.WATER));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (!canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public List<Direction> getAttachableDirections(WorldView world, BlockPos lanternPos) {
        List<Direction> possibleDirections = new ArrayList<>();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos offsetPos = lanternPos.offset(direction);
            if (world.getBlockState(offsetPos).isSideSolidFullSquare(world, offsetPos, direction.getOpposite())) {
                possibleDirections.add(direction);
            }
        }
        return possibleDirections;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(HORIZONTAL_FACING);
        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> VoxelShapes.empty();
        };
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    public static final VoxelShape NORTH_SHAPE = VoxelShapes.union(
            createCuboidShape(5, 0, 3, 11, 7, 9),
            createCuboidShape(6, 7, 4, 10, 9, 8),
            createCuboidShape(7, 12, 3, 9, 14, 16)
    );
    public static final VoxelShape EAST_SHAPE = VoxelShapes.union(
            createCuboidShape(7, 0, 5, 13, 7, 11),
            createCuboidShape(8, 7, 6, 12, 9, 10),
            createCuboidShape(0, 12, 7, 13, 14, 9)
    );
    public static final VoxelShape SOUTH_SHAPE = VoxelShapes.union(
            createCuboidShape(5, 0, 7, 11, 7, 13),
            createCuboidShape(6, 7, 8, 10, 9, 12),
            createCuboidShape(7, 12, 0, 9, 14, 13)
    );
    public static final VoxelShape WEST_SHAPE = VoxelShapes.union(
            createCuboidShape(3, 0, 5, 9, 7, 11),
            createCuboidShape(4, 7, 6, 8, 9, 10),
            createCuboidShape(3, 12, 7, 16, 14, 9)
    );
}
