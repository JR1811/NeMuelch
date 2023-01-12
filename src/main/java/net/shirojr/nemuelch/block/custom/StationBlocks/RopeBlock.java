package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Random;

public class RopeBlock extends Block /*extends BlockWithEntity implements BlockEntityProvider*/ {

    private static final VoxelShape SHAPE_DEFAULT = Block.createCuboidShape(7.25, 0, 7.25, 8.75, 16, 8.75);
    private static final VoxelShape SHAPE_END = Block.createCuboidShape(7.25, 0, 7.25, 8.75, 16, 8.75); //FIXME: create a new model
    public static final BooleanProperty IS_END = NeMuelchProperties.ROPE_END;
    public static final BooleanProperty IS_ANCHOR = NeMuelchProperties.ROPE_ANCHOR;


    public RopeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(IS_END, false). with(IS_ANCHOR, false));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(NeMuelchProperties.ROPE_END) ? SHAPE_END : SHAPE_DEFAULT;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()/*.with(FACING, ctx.getPlayerFacing().getOpposite())*/;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(IS_ANCHOR, IS_END);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);

        BlockState blockState2 = state;
        Iterator<Direction> verticalIterator = Direction.Type.VERTICAL.iterator();


    }
}
