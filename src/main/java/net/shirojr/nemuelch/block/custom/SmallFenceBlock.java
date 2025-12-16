package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.block.util.VariationHolder;


@SuppressWarnings("deprecation")
public class SmallFenceBlock extends HorizontalConnectingBlock implements VariationHolder {
    private final VoxelShape[] cullingShapes;
    private final Variation variation;

    public SmallFenceBlock(Settings settings, Variation variation) {
        super(2f, 2f, 8f, 8f, 8f, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));
        this.cullingShapes = this.createShapes(2.0F, 1.0F, 16.0F, 6.0F, 15.0F);
        this.variation = variation;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED);
        for (BooleanProperty entry : FACING_PROPERTIES.values()) {
            builder.add(entry);
        }
    }

    @Override
    public Variation getVariant() {
        return this.variation;
    }

    @Override
    public Block getBlock() {
        return this;
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_small_fence");
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.cullingShapes[this.getShapeIndex(state)];
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getOutlineShape(state, world, pos, context);
    }

    public boolean canConnect(BlockState state, boolean neighborIsFullSquare, Direction dir) {
        Block block = state.getBlock();
        boolean bl = this.canConnectToFence(state);
        boolean bl2 = block instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, dir);
        return !cannotConnect(state) && neighborIsFullSquare || bl || bl2;
    }

    private boolean canConnectToFence(BlockState state) {
        return state.isOf(this);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos northPos = blockPos.north();
        BlockPos eastPos = blockPos.east();
        BlockPos southPos = blockPos.south();
        BlockPos westPos = blockPos.west();
        BlockState northState = world.getBlockState(northPos);
        BlockState eastState = world.getBlockState(eastPos);
        BlockState southState = world.getBlockState(southPos);
        BlockState westState = world.getBlockState(westPos);
        return getDefaultState()
                .with(NORTH, this.canConnect(northState, northState.isSideSolidFullSquare(world, northPos, Direction.SOUTH), Direction.SOUTH))
                .with(EAST, this.canConnect(eastState, eastState.isSideSolidFullSquare(world, eastPos, Direction.WEST), Direction.WEST))
                .with(SOUTH, this.canConnect(southState, southState.isSideSolidFullSquare(world, southPos, Direction.NORTH), Direction.NORTH))
                .with(WEST, this.canConnect(westState, westState.isSideSolidFullSquare(world, westPos, Direction.EAST), Direction.EAST))
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction.getAxis().getType() == Direction.Type.HORIZONTAL) {
            return state.with(FACING_PROPERTIES.get(direction), this.canConnect(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite()), direction.getOpposite()));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
