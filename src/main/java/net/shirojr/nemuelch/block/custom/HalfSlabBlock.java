package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HalfSlabBlock extends Block implements VariationHolder, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;

    private final Variation variant;

    public HalfSlabBlock(Settings settings, Variation variant) {
        super(settings);
        this.getDefaultState().with(WATERLOGGED, false).with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM);
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
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_half_slab");
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED, FACING, HALF);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        Direction hitDirection = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos();
        boolean hitTopPart = ctx.getHitPos().y - blockPos.getY() > 0.5;
        boolean isTop = hitTopPart || hitDirection == Direction.DOWN;

        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(HALF, isTop ? BlockHalf.TOP : BlockHalf.BOTTOM)
                .with(WATERLOGGED, ctx.getWorld().getFluidState(blockPos).getFluid() == Fluids.WATER);
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
                {0, 0, 0, 16, 8, 8}
        };

        Direction facing = state.get(FACING);
        BlockHalf blockHalf = state.get(HALF);
        VoxelShape result = VoxelShapes.empty();
        for (int[] element : elements) {
            result = VoxelShapes.union(result, createRotatedShape(element, facing, blockHalf));
        }

        return result;
    }

    private VoxelShape createRotatedShape(int[] points, Direction direction, BlockHalf half) {
        int verticalOffset = half == BlockHalf.TOP ? 8 : 0;
        return switch (direction) {
            case NORTH -> createCuboidShape(
                    points[0], points[1] + verticalOffset, points[2],
                    points[3], points[4] + verticalOffset, points[5]
            );
            case SOUTH -> createCuboidShape(
                    16 - points[3], points[1] + verticalOffset, 16 - points[5],
                    16 - points[0], points[4] + verticalOffset, 16 - points[2]
            );
            case WEST -> createCuboidShape(
                    points[2], points[1] + verticalOffset, 16 - points[3],
                    points[5], points[4] + verticalOffset, 16 - points[0]
            );
            case EAST -> createCuboidShape(
                    16 - points[5], points[1] + verticalOffset, points[0],
                    16 - points[2], points[4] + verticalOffset, points[3]
            );
            default -> VoxelShapes.fullCube();
        };
    }
}
