package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.block.entity.custom.CrystalBlockEntity;
import net.shirojr.nemuelch.block.util.VoxelShapeUtil;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CrystalBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;

@SuppressWarnings("deprecation")
public class CrystalBlock extends WallMountedBlock implements BlockEntityProvider, Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty STAGE = NeMuelchProperties.CRYSTAL_STAGE;

    public CrystalBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(WATERLOGGED, false)
                .with(FACING, Direction.NORTH)
                .with(STAGE, 0)
                .with(FACE, WallMountLocation.FLOOR)
        );
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, FACE, WATERLOGGED, STAGE);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalBlockEntity(pos, state);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        ItemStack stack = ctx.getStack();
        if (placementState == null) return null;
        placementState = placementState.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
        OptionalInt stage = CrystalBlockItem.getStage(stack);
        if (stage.isPresent()) {
            placementState = placementState.with(STAGE, stage.getAsInt());
        }
        return placementState;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        int[] points = switch (state.get(STAGE)) {
            case 1, 2 -> new int[]{5, 0, 5, 11, 5, 11};
            case 3, 4 -> new int[]{3, 0, 3, 13, 6, 13};
            default -> new int[]{6, 0, 6, 10, 4, 10};
        };
        return VoxelShapeUtil.createRotatedShape(points, state.get(FACE), state.get(FACING));
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        List<ItemStack> droppedStacks = super.getDroppedStacks(state, builder);
        int stage = state.get(STAGE);
        for (ItemStack entry : droppedStacks) {
            if (!(entry.getItem() instanceof CrystalBlockItem)) continue;
            CrystalBlockItem.setStage(entry, stage);
        }
        return droppedStacks;
    }
}
