package net.shirojr.nemuelch.block.custom.storage;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.item.custom.block.CrateBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@SuppressWarnings("deprecation")
public class CrateBlock extends BlockWithEntity implements Waterloggable {
    public static final EnumProperty<Type> TYPE = NeMuelchProperties.CRATE_TYPE;
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public CrateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(TYPE, Type.SINGLE)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE, FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrateBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return state;
        Direction direction = ctx.getSide();
        if (direction.getAxis().isHorizontal()) {
            state = state.with(TYPE, Type.ANGLED);
        } else {
            direction = ctx.getHorizontalPlayerFacing();
        }
        state = state.with(FACING, direction.getOpposite()).with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
        return state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() instanceof CrateBlockItem) return super.onUse(state, world, pos, player, hand, hit);
        if (!(world.getBlockEntity(pos) instanceof CrateBlockEntity blockEntity)) return ActionResult.FAIL;
        if (world.isClient()) return ActionResult.SUCCESS;
        SimpleInventory blockInventory = blockEntity.getInventory(hit.getPos());

        if (stackInHand.isEmpty()) {
            ItemStack retrievedStack = ItemStack.EMPTY;
            for (int i = blockInventory.stacks.size() - 1; i >= 0; i--) {
                ItemStack entryStack = blockInventory.getStack(i);
                if (entryStack.isEmpty()) continue;
                retrievedStack = entryStack.copy();
                blockInventory.setStack(i, ItemStack.EMPTY);
                break;
            }
            if (!retrievedStack.isEmpty()) {
                player.getInventory().offerOrDrop(retrievedStack);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        } else {
            ItemStack leftOverStack = blockInventory.addStack(stackInHand.copy());
            if (ItemStack.areEqual(stackInHand, leftOverStack)) {
                return ActionResult.FAIL;
            } else {
                player.setStackInHand(hand, leftOverStack);
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS);
                }
                return ActionResult.SUCCESS;
            }
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.getBlock().equals(newState.getBlock()) && world.getBlockEntity(pos) instanceof CrateBlockEntity blockEntity) {
            blockEntity.onBroken();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        double height = switch (state.get(TYPE)) {
            case SINGLE -> 8;
            case DOUBLE -> 15;
            case ANGLED -> 14;
        };
        if (state.get(FACING) == Direction.NORTH || state.get(FACING) == Direction.SOUTH) {
            return createCuboidShape(3, 0, 0, 13, height, 16);
        } else {
            return createCuboidShape(0, 0, 3, 16, height, 13);
        }
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        if(state.get(TYPE) == Type.ANGLED) return VoxelShapes.empty();
        return super.getCullingShape(state, world, pos);
    }

    public static boolean upgrade(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.get(TYPE) != Type.SINGLE) return false;
        if (!world.isClient()) {
            world.setBlockState(pos, state.with(TYPE, Type.DOUBLE));
        }
        return true;
    }


    public enum Type implements StringIdentifiable {
        SINGLE(6),
        DOUBLE(12),
        ANGLED(6);

        private final int space;

        Type(int space) {
            this.space = space;
        }

        public int getSpace() {
            return space;
        }

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
