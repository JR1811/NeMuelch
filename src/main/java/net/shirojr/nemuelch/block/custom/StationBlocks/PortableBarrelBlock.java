package net.shirojr.nemuelch.block.custom.StationBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class PortableBarrelBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public PortableBarrelBlock(Settings settings) {
        super(settings);
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
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        return ActionResult.PASS;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(FACING)) {
            case NORTH:
                return SHAPE_N;
            default:
                return SHAPE_N;
        }
    }

    //TODO: clean those numbers up a bit
    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(3.8000000000000007, 9.399999999999993, 3.8000000000000007, 12.2, 10.399999999999993, 12.2),
            Block.createCuboidShape(3.8000000000000007, 5.399999999999995, 3.8000000000000007, 12.2, 6.399999999999993, 12.2),
            Block.createCuboidShape(3.8000000000000007, 2.4000000000000004, 3.8000000000000007, 12.2, 3.4000000000000012, 12.2),
            Block.createCuboidShape(4, 1.4000000000000004, 3.999999999999999, 12, 11.399999999999993, 12),
            Block.createCuboidShape(4.3, 11.399999999999993, 10.3, 11.7, 12.399999999999993, 11.7),
            Block.createCuboidShape(4.3, 11.399999999999993, 4.3, 11.7, 12.399999999999993, 5.699999999999999),
            Block.createCuboidShape(10.3, 11.399999999999993, 5.700000000000001, 11.7, 12.399999999999993, 10.299999999999999),
            Block.createCuboidShape(4.3, 11.399999999999993, 5.700000000000001, 5.7, 12.399999999999993, 10.299999999999999),
            Block.createCuboidShape(4.3, 0.4000000000000007, 4.299999999999999, 11.7, 1.4000000000000004, 11.7),
            Block.createCuboidShape(5.3, 4.440892098500626e-16, 5.300000000000001, 10.7, 0.4000000000000007, 10.7)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
}
