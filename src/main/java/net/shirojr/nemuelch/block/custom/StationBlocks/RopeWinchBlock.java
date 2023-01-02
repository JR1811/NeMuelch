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
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;
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
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(0, 0, 0, 16, 12, 6),
            Block.createCuboidShape(0, 0, 6, 16, 6, 12),
            Block.createCuboidShape(5, 0, 20, 11, 12, 28),
            Block.createCuboidShape(6.7, 9.2, -12, 8.3, 10.8, 25.5)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(0, 0, 0, 6, 12, 16),
            Block.createCuboidShape(-12, 9.2, 7.7, 20, 10.8, 9.3),
            Block.createCuboidShape(6, 0, 0, 12, 6, 16),
            Block.createCuboidShape(20, 0, 5, 28, 12, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(0, 0, 10, 16, 12, 16),
            Block.createCuboidShape(7.7, 9.2, -4, 9.3, 10.8, 28),
            Block.createCuboidShape(0, 0, 4, 16, 6, 10),
            Block.createCuboidShape(5, 0, -12, 11, 12, -4)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(10, 0, 0, 16, 12, 16),
            Block.createCuboidShape(-4, 9.2, 6.7, 28, 10.8, 8.3),
            Block.createCuboidShape(4, 0, 0, 10, 6, 16),
            Block.createCuboidShape(-12, 0, 5, -4, 12, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    static {
        FACING = Properties.HORIZONTAL_FACING;
        ROPED = NeMuelchProperties.ROPED;
    }
}
