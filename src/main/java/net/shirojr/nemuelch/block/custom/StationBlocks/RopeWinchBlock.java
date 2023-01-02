package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
import net.shirojr.nemuelch.block.entity.PestcaneStationBlockEntity;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class RopeWinchBlock extends Block /*extends BlockWithEntity implements BlockEntityProvider*/ {


    public static final DirectionProperty FACING;
    public static final BooleanProperty ROPED;

    public RopeWinchBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(ROPED, false));
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(ROPED)) {
            state.with(NeMuelchProperties.ROPED, false);
        }
        else state.with(NeMuelchProperties.ROPED, true);

        world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 2f, 1f, true);
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
        return super.onUse(state, world, pos, player, hand, hit);
    }

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

    static {
        FACING = Properties.HORIZONTAL_FACING;
        ROPED = NeMuelchProperties.ROPED;
    }
}
