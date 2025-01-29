package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RopeBlock extends Block {

    private static final VoxelShape SHAPE_DEFAULT = Block.createCuboidShape(7.25, 0, 7.25, 8.75, 16, 8.75);
    public static final BooleanProperty IS_ANCHOR = NeMuelchProperties.ROPE_ANCHOR;

    public RopeBlock(Settings settings) {
        super(settings
                .ticksRandomly()
                .sounds(BlockSoundGroup.WOOL));
        this.setDefaultState(this.stateManager.getDefaultState().with(IS_ANCHOR, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE_DEFAULT;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos posAbove = ctx.getBlockPos().up();
        return this.getDefaultState().with(IS_ANCHOR, world.getBlockState(posAbove).isFullCube(world, posAbove));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockPos posAbove = pos.up();
        state = state.with(IS_ANCHOR, world.getBlockState(posAbove).isFullCube(world, posAbove));
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IS_ANCHOR);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getBlockState(pos.up()).getBlock().equals(NeMuelchBlocks.ROPE)) return;
        if (world.getBlockState(pos.up()).getBlock().equals(NeMuelchBlocks.ROPER)) return;
        if (state.get(NeMuelchProperties.ROPE_ANCHOR)) return;
        if (state.isSideSolidFullSquare(world, pos.up(), Direction.DOWN)) return;
        if (world.getBlockState(pos.up()).isIn(BlockTags.FENCES)) return;
        world.breakBlock(pos, false);
    }
}
