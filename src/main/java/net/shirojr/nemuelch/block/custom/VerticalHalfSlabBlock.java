package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.util.Variation;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class VerticalHalfSlabBlock extends AbstractVariationBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public VerticalHalfSlabBlock(Settings settings, Variation variant) {
        super(settings, variant);
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public Identifier getBaseModel() {
        return NeMuelch.getId("block/base_vertical_half_slab");
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return createRotatedShape(new int[]{0, 0, 0, 8, 16, 8}, state.get(FACING));
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState == null) return null;

        Vec3d hitPos = ctx.getHitPos();
        double x = (int) hitPos.getX() - hitPos.getX();
        if (x < 0) x = 1 - Math.abs(x);
        double z = (int) hitPos.getZ() - hitPos.getZ();
        if (z < 0) z = 1 - Math.abs(z);
        Direction side = ctx.getSide();

        LoggerUtil.devLogger("Hit Block at X: %s | Z: %s | Side: %s".formatted(x, z, side));

        Direction facing = determineFacing(side, x, z);
        return placementState.with(FACING, facing);
    }

    private Direction determineFacing(Direction side, double x, double z) {
        boolean isLowerX = x < 0.5;
        boolean isLowerZ = z < 0.5;

        return switch (side) {
            case UP, DOWN -> {
                if (isLowerX && isLowerZ) yield Direction.SOUTH;
                if (isLowerX) yield Direction.EAST;
                if (isLowerZ) yield Direction.WEST;
                yield Direction.NORTH;
            }
            case NORTH -> isLowerX ? Direction.SOUTH : Direction.WEST;
            case EAST -> isLowerZ ? Direction.WEST : Direction.NORTH;
            case SOUTH -> isLowerX ? Direction.EAST : Direction.NORTH;
            case WEST -> isLowerZ ? Direction.SOUTH : Direction.EAST;
        };
    }
}
