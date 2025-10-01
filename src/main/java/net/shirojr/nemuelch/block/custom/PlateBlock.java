package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;

@SuppressWarnings("deprecation")
public class PlateBlock extends AbstractVariationBlock {
    public static final DirectionProperty FACING = Properties.FACING;

    public PlateBlock(Settings settings, Variation variant) {
        super(settings, variant);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
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
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_plate");
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        return placementState.with(FACING, ctx.getPlayerLookDirection());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int[][] elements = {
                {0, 0, 0, 16, 16, 2}
        };

        Direction facing = state.get(FACING);
        VoxelShape result = VoxelShapes.empty();
        for (int[] element : elements) {
            result = VoxelShapes.union(result, createRotatedShape(element, facing));
        }

        return result;
    }
}
