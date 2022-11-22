package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
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
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.block.entity.PestcaneStationBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.stream.Stream;

public class PestcaneStationBlock extends BlockWithEntity implements BlockEntityProvider {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public PestcaneStationBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
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

        builder.add(FACING, LIT);
    }


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {

        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PestcaneStationBlockEntity) {
                ItemScatterer.spawn(world, pos, (PestcaneStationBlockEntity)blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PestcaneStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.PESTCANE_STATION, PestcaneStationBlockEntity::tick);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {

        if(state.get(LIT)) {

            // pos at bottom center of block
            double x = (double)pos.getX() + 0.5;
            double y = (double)pos.getY();
            double z = (double)pos.getZ() + 0.5;

            if (random.nextDouble() < 0.1) {

                world.playSound(x, y, z, SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS,
                        1.0F, 1.0F, false);
            }


            world.addParticle(ParticleTypes.LAVA, x, y + 1.2, z, 0.0, 2.0, 0.0);
        }
    }

    //region voxel shape
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
    //endregions
}
