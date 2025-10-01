package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;

@SuppressWarnings("deprecation")
public class CenteredHalfSlabBlock extends AbstractVariationBlock {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;

    public CenteredHalfSlabBlock(Settings settings, Variation variant) {
        super(settings, variant);
        this.setDefaultState(this.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_centered_half_slab");
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        return placementState.with(AXIS, ctx.getSide().getAxis());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction.Axis axis = state.get(AXIS);
        return createRotatedAxisShape(new int[] {4, 0, 4, 12, 16, 12}, axis);
    }
}
