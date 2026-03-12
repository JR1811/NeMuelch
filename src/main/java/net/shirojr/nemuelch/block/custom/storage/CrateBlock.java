package net.shirojr.nemuelch.block.custom.storage;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LeadItem;
import net.minecraft.particle.ParticleTypes;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
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
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.CRATE, CrateBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return state;
        Direction direction = ctx.getSide().getOpposite();

        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos().offset(direction);
        if (direction.getAxis().isHorizontal() && world.getBlockState(pos).isSideSolidFullSquare(world, pos, direction)) {
            state = state.with(TYPE, Type.ANGLED);
        } else {
            direction = ctx.getHorizontalPlayerFacing().getOpposite();
        }
        state = state.with(FACING, direction).with(WATERLOGGED, world.getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
        return state;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(TYPE) == Type.ANGLED) {
            Direction facing = state.get(FACING);
            return world.getBlockState(pos.offset(facing)).isSideSolidFullSquare(world, pos, facing);
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) return super.onUse(state, world, pos, player, hand, hit);
        ItemStack stackInHand = player.getMainHandStack();
        if (stackInHand.getItem() instanceof CrateBlockItem) {
            return super.onUse(state, world, pos, player, hand, hit);
        }
        if (!(world.getBlockEntity(pos) instanceof CrateBlockEntity blockEntity)) return ActionResult.PASS;
        for (MobEntity entity : world.getNonSpectatingEntities(MobEntity.class, new Box(player.getBlockPos()).expand(10))) {
            if (entity.getHoldingEntity() == player && blockEntity.canAddEntity(entity)) {
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS);
                    serverWorld.spawnParticles(ParticleTypes.CLOUD,
                            entity.getBlockPos().toCenterPos().getX(),
                            entity.getBlockPos().toCenterPos().getY(),
                            entity.getBlockPos().toCenterPos().getZ(),
                            10, 1, 1, 1, 0.01);
                }
                entity.detachLeash(true, world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS));
                blockEntity.setStoredEntity(entity, true);
                blockEntity.releaseBottomInventory();
                blockEntity.releaseTopInventory();
                return ActionResult.SUCCESS;
            }
        }

        Vec3d localHitPos = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        SimpleInventory blockInventory = blockEntity.getInventory(localHitPos);
        if (stackInHand.isEmpty()) {
            ItemStack retrievedStack = ItemStack.EMPTY;
            for (int i = blockInventory.stacks.size() - 1; i >= 0; i--) {
                ItemStack entryStack = blockInventory.getStack(i);
                if (entryStack.isEmpty()) continue;
                retrievedStack = entryStack.copy();
                if (!world.isClient()) blockInventory.setStack(i, ItemStack.EMPTY);
                break;
            }
            if (!retrievedStack.isEmpty()) {
                blockEntity.markDirty();
                if (world instanceof ServerWorld serverWorld) {
                    player.getInventory().offerOrDrop(retrievedStack);
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }
        if(stackInHand.getItem() instanceof LeadItem && !player.isSneaking()) {
            MobEntity mobEntity = blockEntity.spawnStoredEntity(blockEntity.getPos().up().toCenterPos());
            if (mobEntity != null) {
                mobEntity.attachLeash(player, world instanceof ServerWorld);
                blockEntity.setStoredEntity(null, false);
                if (world instanceof ServerWorld serverWorld) {
                    BlockPos effectPos = blockEntity.getPos();
                    serverWorld.playSound(null, effectPos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS);
                    serverWorld.spawnParticles(ParticleTypes.CLOUD,
                            effectPos.toCenterPos().getX(), effectPos.toCenterPos().getY(), effectPos.toCenterPos().getZ(),
                            10, 1, 1, 1, 0.01);
                    if (!player.isCreative()) {
                        stackInHand.decrement(1);
                    }
                }
                return ActionResult.SUCCESS;
            }
        }
        if (blockEntity.canAddItem(blockInventory, stackInHand)) {
            if (world.isClient()) return ActionResult.SUCCESS;
            ItemStack leftOverStack = blockInventory.addStack(stackInHand.copy());
            blockEntity.markDirty();
            if (ItemStack.areEqual(stackInHand, leftOverStack)) {
                return ActionResult.PASS;
            } else {
                if (world instanceof ServerWorld serverWorld) {
                    if (!player.isCreative()) {
                        player.setStackInHand(hand, leftOverStack);
                    }
                    serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS);
                }
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (state.get(TYPE) == Type.ANGLED) {
            if (!canPlaceAt(state, world, pos)) {
                BlockState newState = state.with(TYPE, Type.SINGLE);
                world.setBlockState(pos, newState, NOTIFY_ALL);
                return newState;
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.getBlockEntity(pos) instanceof CrateBlockEntity blockEntity) {
            if (!state.getBlock().equals(newState.getBlock())) {
                blockEntity.onBroken();
            } else if (state.get(TYPE) == Type.DOUBLE && newState.get(TYPE) != Type.DOUBLE) {
                blockEntity.releaseTopInventory();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        double height = switch (state.get(TYPE)) {
            case SINGLE -> 8;
            case DOUBLE, ENTITY -> 15;
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
        if (state.get(TYPE) == Type.ANGLED) return VoxelShapes.empty();
        return super.getCullingShape(state, world, pos);
    }

    public static boolean upgrade(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.get(TYPE) != Type.SINGLE) return false;
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setBlockState(pos, state.with(TYPE, Type.DOUBLE));
            serverWorld.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS);
        }
        return true;
    }


    public enum Type implements StringIdentifiable {
        SINGLE, DOUBLE, ANGLED, ENTITY;

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
