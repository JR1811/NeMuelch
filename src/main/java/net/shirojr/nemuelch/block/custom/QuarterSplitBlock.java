package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.supportItem.TntStickItem;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation"})
public class QuarterSplitBlock extends Block implements Waterloggable {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty PARTS = NeMuelchProperties.QUARTER_SPLIT_PARTS;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public QuarterSplitBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH).with(WATERLOGGED, false).with(PARTS, 4));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof ShearsItem)) return super.onUse(state, world, pos, player, hand, hit);

        if (!world.isClient()) {
            if (state.get(PARTS) <= 1) {
                world.breakBlock(pos, false);
            } else {
                BlockState newState = state.with(PARTS, state.get(PARTS) - 1);
                world.setBlockState(pos, newState);
                world.updateNeighbors(pos, world.getBlockState(pos).getBlock());

                ItemScatterer.spawn(world, pos.getX(), pos.getY() + 1, pos.getZ(), new ItemStack(NeMuelchItems.TNT_STICK));
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, PARTS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = VoxelShapes.empty();

        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0, 0.5, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0, 1, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.5, 1, 1, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.5, 0.5, 1, 1));

        return shape;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean isWaterlogged = ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER);
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(WATERLOGGED, isWaterlogged);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
