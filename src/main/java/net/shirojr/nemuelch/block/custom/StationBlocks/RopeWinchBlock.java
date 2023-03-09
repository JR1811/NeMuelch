package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.block.entity.RopeWinchBlockEntity;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class RopeWinchBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ROPED = NeMuelchProperties.ROPED;

    public RopeWinchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ROPED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RopeWinchBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> state.get(ROPED) ? SHAPE_E_ROPED : SHAPE_E;
            case SOUTH -> state.get(ROPED) ? SHAPE_S_ROPED : SHAPE_S;
            case WEST -> state.get(ROPED) ? SHAPE_W_ROPED : SHAPE_W;
            default -> state.get(ROPED) ? SHAPE_N_ROPED : SHAPE_N;
        };
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing()/*.getOpposite()*/).with(ROPED, false);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING))).with(ROPED, state.get(ROPED));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING))).with(ROPED, state.get(ROPED));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROPED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.ROPER_STATION, RopeWinchBlockEntity::tick);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RopeWinchBlockEntity) {
                ItemScatterer.spawn(world, pos, (RopeWinchBlockEntity)blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        Direction stationDirection = state.get(RopeWinchBlock.FACING);
        BlockPos ropePos = pos.mutableCopy().offset(stationDirection, 1).down();

        if (world.getBlockState(ropePos).getBlock() == NeMuelchBlocks.ROPE) {
            world.setBlockState(ropePos, NeMuelchBlocks.ROPE
                            .getDefaultState().with(NeMuelchProperties.ROPE_ANCHOR, false),
                    Block.NOTIFY_LISTENERS);
        }

        if (world.getBlockEntity(pos) instanceof RopeWinchBlockEntity entity) {
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY() + 1, pos.getZ(),
                    new ItemStack(NeMuelchBlocks.ROPE.asItem(), entity.getStack(0).getCount()),
                    0.0, 0.4, 0.0));
        }


        super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }

    //region VoxelShapes
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(6.5, 8.5, -12, 9.5, 11.5, 28),
            Block.createCuboidShape(6, 0, 2, 10, 8.5, 26),
            Block.createCuboidShape(0, 0, 0.5, 16, 12, 5),
            Block.createCuboidShape(0, 0, 5, 16, 6, 10.5)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_N_ROPED = Stream.of(
            Block.createCuboidShape(7.25, 0, -8.75, 8.75, 9, -7.25),
            Block.createCuboidShape(6.5, 8.5, -12, 9.5, 11.5, 28),
            Block.createCuboidShape(6, 0, 2, 10, 8.5, 26),
            Block.createCuboidShape(0, 0, 0.5, 16, 12, 5),
            Block.createCuboidShape(0, 0, 5, 16, 6, 10.5)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(-12, 8.5, 6.5, 28, 11.5, 9.5),
            Block.createCuboidShape(2, 0, 6, 26, 8.5, 10),
            Block.createCuboidShape(0.5, 0, 0, 5, 12, 16),
            Block.createCuboidShape(5, 0, 0, 10.5, 6, 16)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_W_ROPED = Stream.of(
            Block.createCuboidShape(-8.75, 0, 7.25, -7.25, 9, 8.75),
            Block.createCuboidShape(-12, 8.5, 6.5, 28, 11.5, 9.5),
            Block.createCuboidShape(2, 0, 6, 26, 8.5, 10),
            Block.createCuboidShape(0.5, 0, 0, 5, 12, 16),
            Block.createCuboidShape(5, 0, 0, 10.5, 6, 16)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(6.5, 8.5, -12, 9.5, 11.5, 28),
            Block.createCuboidShape(6, 0, -10, 10, 8.5, 14),
            Block.createCuboidShape(0, 0, 11, 16, 12, 15.5),
            Block.createCuboidShape(0, 0, 5.5, 16, 6, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_S_ROPED = Stream.of(
            Block.createCuboidShape(7.25, 0, 23.25, 8.75, 9, 24.75),
            Block.createCuboidShape(6.5, 8.5, -12, 9.5, 11.5, 28),
            Block.createCuboidShape(6, 0, -10, 10, 8.5, 14),
            Block.createCuboidShape(0, 0, 11, 16, 12, 15.5),
            Block.createCuboidShape(0, 0, 5.5, 16, 6, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(-12, 8.5, 6.5, 28, 11.5, 9.5),
            Block.createCuboidShape(-10, 0, 6, 14, 8.5, 10),
            Block.createCuboidShape(11, 0, 0, 15.5, 12, 16),
            Block.createCuboidShape(5.5, 0, 0, 11, 6, 16)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_E_ROPED = Stream.of(
            Block.createCuboidShape(23.25, 0, 7.25, 24.75, 9, 8.75),
            Block.createCuboidShape(-12, 8.5, 6.5, 28, 11.5, 9.5),
            Block.createCuboidShape(-10, 0, 6, 14, 8.5, 10),
            Block.createCuboidShape(11, 0, 0, 15.5, 12, 16),
            Block.createCuboidShape(5.5, 0, 0, 11, 6, 16)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    //endregion
}
