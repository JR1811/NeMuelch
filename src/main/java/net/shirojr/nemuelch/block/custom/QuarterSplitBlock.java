package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

public class QuarterSplitBlock extends Block implements Waterloggable {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty PARTS = NeMuelchProperties.QUARTER_SPLIT_PARTS;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public QuarterSplitBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(PARTS, 4));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, PARTS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = VoxelShapes.empty();

        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean isWaterlogged = ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER);
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(WATERLOGGED, isWaterlogged);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    //region VoxelShapes
    private static VoxelShape NORTH_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();

        // shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    private static VoxelShape EAST_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();

        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        // shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    private static VoxelShape SOUTH_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();

        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        // shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    private static VoxelShape WEST_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();

        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        // shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    //endregion
}
