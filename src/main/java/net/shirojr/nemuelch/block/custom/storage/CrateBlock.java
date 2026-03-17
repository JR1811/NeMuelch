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
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.NeMuelch;
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
    private final String prefixMaterial;
    private final Block baseMaterial;

    public CrateBlock(Settings settings, String prefixMaterial, Block baseMaterial) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(TYPE, Type.SINGLE)
                .with(FACING, Direction.NORTH)
                .with(WATERLOGGED, false)
        );
        this.prefixMaterial = prefixMaterial;
        this.baseMaterial = baseMaterial;
    }

    public String getMaterialPrefix() {
        return prefixMaterial;
    }

    public Block getBaseMaterial() {
        return baseMaterial;
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
                blockEntity.addStoredEntity(entity);
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
        if (stackInHand.getItem() instanceof LeadItem && !player.isSneaking()) {
            if (blockEntity.hasStoredEntity()) {
                blockEntity.releaseStoredEntity(world, blockEntity.getPos().toCenterPos(), player, stackInHand);
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
            case SINGLE, ENTITY -> 8;
            case DOUBLE -> 16;
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

    public static boolean changeType(World world, BlockPos pos, Type type) {
        BlockState state = world.getBlockState(pos);
        if (!state.contains(TYPE) || state.get(TYPE) == type) return false;
        //if (state.get(TYPE) == Type.ENTITY && type != Type.ENTITY) {
        if (world.getBlockEntity(pos) instanceof CrateBlockEntity blockEntity) {
            if (blockEntity.hasStoredEntity()) return false;
        }
        //}
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setBlockState(pos, state.with(TYPE, type));
            serverWorld.playSound(null, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS);
        }
        return true;
    }


    public enum Type implements StringIdentifiable {
        SINGLE("crate_single"),
        DOUBLE("crate_double"),
        ANGLED("crate_angled"),
        ENTITY("crate_entity");

        private final Identifier parentModelName;

        Type(String parentModelName) {
            this.parentModelName = NeMuelch.getId(parentModelName);
        }

        public Identifier getParentModel() {
            return parentModelName;
        }

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
