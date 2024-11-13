package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.NeMuelchBlockEntities;
import net.shirojr.nemuelch.block.entity.WateringCanBlockEntity;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.util.NeMuelchProperties;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;
import org.jetbrains.annotations.Nullable;


public class WateringCanBlock extends BlockWithEntity implements Waterloggable, BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty FILLED = NeMuelchProperties.FILLED;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<WateringCanHelper.ItemMaterial> MATERIAL = NeMuelchProperties.MATERIAL;

    public WateringCanBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(FILLED, false)
                .with(MATERIAL, WateringCanHelper.ItemMaterial.COPPER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FILLED, WATERLOGGED, MATERIAL);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> EAST_SHAPE();
            case SOUTH -> SOUTH_SHAPE();
            case WEST -> WEST_SHAPE();
            default -> NORTH_SHAPE();
        };
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean isWaterlogged = ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER);
        boolean isFull = WateringCanHelper.readNbtFillState(ctx.getStack()) >= WateringCanHelper.getItemMaterial(ctx.getStack()).getCapacity();

        return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(WATERLOGGED, isWaterlogged).with(FILLED, isFull)
                .with(MATERIAL, WateringCanHelper.getItemMaterial(ctx.getStack()));
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof WateringCanBlockEntity blockEntity)) return ActionResult.PASS;
        ItemStack itemStack;
        switch (state.get(NeMuelchProperties.MATERIAL)) {
            case IRON -> itemStack = new ItemStack(NeMuelchItems.WATERING_CAN_IRON);
            case GOLD -> itemStack = new ItemStack(NeMuelchItems.WATERING_CAN_GOLD);
            case DIAMOND -> itemStack = new ItemStack(NeMuelchItems.WATERING_CAN_DIAMOND);
            default -> itemStack = new ItemStack(NeMuelchItems.WATERING_CAN_COPPER);
        }
        if (world instanceof ServerWorld) {
            world.breakBlock(pos, false);
            WateringCanHelper.writeNbtFillState(itemStack, blockEntity.getFillState());
            BlockPos newScatterPos = pos.up();
            ItemScatterer.spawn(world, newScatterPos.getX(), newScatterPos.getY(), newScatterPos.getZ(), itemStack);
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WateringCanBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.WATERING_CAN, WateringCanBlockEntity::tick);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED)) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.getBlockEntity(pos) instanceof WateringCanBlockEntity blockEntity && !world.isClient()) {
            blockEntity.setMaterial(WateringCanHelper.getItemMaterial(itemStack));
            blockEntity.setFillState(WateringCanHelper.readNbtFillState(itemStack));

            if (!state.get(WATERLOGGED)) return;
            blockEntity.setShouldFill(true);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.get(NeMuelchProperties.FILLED) && world instanceof ServerWorld serverWorld) {
            world.setBlockState(pos, Blocks.WATER.getDefaultState());
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_ANVIL_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        super.onBreak(world, pos, state, player);
    }

    //region voxelshapes
    private static VoxelShape NORTH_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0, 0.1875, 0.75, 0.75, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0, 0.625, 0.6875, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.125, 0.8125, 0.625, 0.75, 1));

        return shape;
    }

    private static VoxelShape EAST_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0, 0.25, 0.8125, 0.75, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 0, 0.375, 1, 0.6875, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0.125, 0.375, 0.1875, 0.75, 0.625));

        return shape;
    }

    private static VoxelShape SOUTH_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0, 0.1875, 0.75, 0.75, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0.8125, 0.625, 0.6875, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.125, 0, 0.625, 0.75, 0.1875));

        return shape;
    }

    private static VoxelShape WEST_SHAPE() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.1875, 0, 0.25, 0.8125, 0.75, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0, 0, 0.375, 0.1875, 0.6875, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.8125, 0.125, 0.375, 1, 0.75, 0.625));

        return shape;
    }
    //endregion
}
