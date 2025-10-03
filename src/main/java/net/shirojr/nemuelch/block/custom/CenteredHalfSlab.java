package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CenteredHalfSlab extends AbstractVariationBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;

    public CenteredHalfSlab(Settings settings, Variation variant) {
        super(settings, variant);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(HALF, BlockHalf.BOTTOM));
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_centered_half_slab");
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, HALF);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;
        Direction hitDirection = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos();
        boolean hitTopPart = ctx.getHitPos().y - blockPos.getY() > 0.5;
        boolean isTop = hitTopPart || hitDirection == Direction.DOWN;

        return placementState
                .with(FACING, ctx.getHorizontalPlayerFacing())
                .with(HALF, isTop ? BlockHalf.TOP : BlockHalf.BOTTOM);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return createCardinalRotatedShape(new int[]{4, 0, 0, 12, 8, 16}, state.get(FACING), state.get(HALF));
    }
}
