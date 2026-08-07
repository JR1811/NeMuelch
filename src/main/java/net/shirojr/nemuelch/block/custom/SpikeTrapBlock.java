package net.shirojr.nemuelch.block.custom;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.data.client.ModelIds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.block.entity.custom.SpikeTrapBlockEntity;
import net.shirojr.nemuelch.block.util.VoxelShapeUtil;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.item.custom.supportItem.SoapItem;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class SpikeTrapBlock extends Block implements BlockEntityProvider, Waterloggable {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final EnumProperty<State> STATE = NeMuelchProperties.SPIKE_TRAP_STATE;

    private static final int GROUP_RETRACT_PROPAGATION_SPEED = 6;
    private static final int GROUP_EXPOSE_PROPAGATION_SPEED = 2;
    private static final int MAX_GROUP_SIZE = 512;

    private static final Function<BlockState, VoxelShape> SMALL_SHAPE = state ->
            VoxelShapeUtil.createRotatedShape(new int[]{0, 0, 0, 16, 2, 16}, state.get(FACING));

    public SpikeTrapBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.UP)
                .with(WATERLOGGED, false)
                .with(STATE, State.DEFAULT)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, WATERLOGGED, STATE);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpikeTrapBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand.equals(Hand.OFF_HAND)) {
            return super.onUse(state, world, pos, player, hand, hit);
        }
        if (!(world.getBlockEntity(pos) instanceof SpikeTrapBlockEntity blockEntity)) {
            return super.onUse(state, world, pos, player, hand, hit);
        }
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() instanceof PotionItem && State.isExposed(state)) {
            Potion potionInHand = PotionUtil.getPotion(stack);
            if (blockEntity.canApplyPotion(potionInHand)) {
                if (world instanceof ServerWorld serverWorld) {
                    blockEntity.setPotion(potionInHand);
                    world.setBlockState(pos, state.with(STATE, State.EXPOSED_WITH_POTION));
                    if (!player.isCreative() && !player.isSpectator()) {
                        stack.decrement(1);
                        player.getInventory().offerOrDrop(Items.GLASS_BOTTLE.getDefaultStack());
                    }
                    serverWorld.playSound(null, pos, SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.BLOCKS, 2f, 1f);
                }
                return ActionResult.SUCCESS;
            }
        }
        if (stack.isIn(NeMuelchTags.Items.SOAP) && state.get(STATE) == State.EXPOSED_WITH_POTION) {
            if (world instanceof ServerWorld serverWorld) {
                blockEntity.clear();
                if (!player.isCreative() && !player.isSpectator()) {
                    SoapItem.decrementCoating(stack);
                }
                serverWorld.setBlockState(pos, state.with(STATE, State.EXPOSED));
                serverWorld.playSound(null, pos, NeMuelchSounds.PULL_UP, SoundCategory.BLOCKS, 2f, 1.2f);
            }
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (!state.contains(FACING)) return super.canPlaceAt(state, world, pos);
        Direction direction = state.get(FACING);
        BlockPos neighborPos = pos.offset(direction.getOpposite());
        BlockState neighborState = world.getBlockState(neighborPos);
        return neighborState.isSideSolidFullSquare(world, neighborPos, direction.getOpposite());
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAlways(pos.offset(direction), this);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (State.isExposed(state)) {
            return VoxelShapes.fullCube();
        } else {
            return state.getCollisionShape(world, pos);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SMALL_SHAPE.apply(state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = world.getFluidState(blockPos);
        boolean receivingRedstonePower = world.isReceivingRedstonePower(blockPos);
        State trapState = receivingRedstonePower ? State.getExposedState(world, blockPos) : State.DEFAULT;
        return this.getDefaultState()
                .with(FACING, direction)
                .with(WATERLOGGED, fluidState.getFluid().equals(Fluids.WATER))
                .with(STATE, trapState);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (!canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return !State.isExposed(state);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!State.isExposed(state)) return;
        livingEntity.slowMovement(state, new Vec3d(0.6F, 0.1F, 0.6F));
        if (world instanceof ServerWorld serverWorld) {
            if (livingEntity.lastRenderX != livingEntity.getX() || livingEntity.lastRenderY != livingEntity.getY() || livingEntity.lastRenderZ != livingEntity.getZ()) {
                double xDifference = Math.abs(livingEntity.getX() - livingEntity.lastRenderX);
                double yDifference = Math.abs(livingEntity.getY() - livingEntity.lastRenderY);
                double zDifference = Math.abs(livingEntity.getZ() - livingEntity.lastRenderZ);
                if (xDifference >= 0.003F || yDifference > 0F || zDifference >= 0.003F) {
                    livingEntity.damage(NeMuelchDamageTypes.of(serverWorld, NeMuelchDamageTypes.PIERCING), 2.0F);
                    if (serverWorld.getBlockEntity(pos) instanceof SpikeTrapBlockEntity blockEntity && blockEntity.hasPotion()) {
                        blockEntity.applyEffects(livingEntity);
                    } else {
                        if (!livingEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
                            livingEntity.addStatusEffect(
                                    new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 1,
                                            false, false, true)
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (!(world instanceof ServerWorld serverWorld)) return;
        this.refreshGroupStateBfs(serverWorld, pos, state.get(FACING));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        boolean groupPowered = this.isGroupPoweredBfs(world, pos, state.get(FACING));
        boolean exposed = State.isExposed(state);
        if (groupPowered == exposed) return;
        State.cycle(world, pos, true);
        SoundEvent soundEvent = groupPowered ? NeMuelchSounds.SPIKE_TRAP_EXPOSE : NeMuelchSounds.SPIKE_TRAP_RETRACT;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1, 1);
    }

    public ActionResult onAttackBlock(LivingEntity attacker, BlockState state) {
        if (attacker instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) {
            return ActionResult.PASS;
        }
        if (State.isExposed(state)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private boolean isGroupPoweredBfs(ServerWorld world, BlockPos originPos, Direction facing) {
        HashSet<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(originPos);
        visited.add(originPos);
        while (!queue.isEmpty() && visited.size() <= MAX_GROUP_SIZE) {
            BlockPos entryPos = queue.poll();
            if (world.isReceivingRedstonePower(entryPos)) return true;
            for (Direction direction : perpendicularDirections(facing)) {
                BlockPos nextPos = entryPos.offset(direction);
                if (visited.contains(nextPos)) continue;
                BlockState nextState = world.getBlockState(nextPos);
                if (nextState.getBlock() instanceof SpikeTrapBlock && nextState.get(FACING) == facing) {
                    visited.add(nextPos);
                    queue.add(nextPos);
                }
            }
        }
        return false;
    }

    private void refreshGroupStateBfs(ServerWorld world, BlockPos originPos, Direction facing) {
        Object2IntOpenHashMap<BlockPos> visitedWithDepth = new Object2IntOpenHashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> fullGroup = new ArrayList<>();
        boolean groupPowered = false;

        queue.add(originPos);
        visitedWithDepth.put(originPos, 0);
        while (!queue.isEmpty() && visitedWithDepth.size() <= MAX_GROUP_SIZE) {
            BlockPos entryPos = queue.poll();
            int depth = visitedWithDepth.get(entryPos);
            fullGroup.add(entryPos);
            if (!groupPowered && world.isReceivingRedstonePower(entryPos)) {
                groupPowered = true;
            }
            for (Direction direction : perpendicularDirections(facing)) {
                BlockPos nextPos = entryPos.offset(direction);
                if (visitedWithDepth.containsKey(nextPos)) continue;
                BlockState nextState = world.getBlockState(nextPos);
                if (nextState.getBlock() instanceof SpikeTrapBlock && nextState.get(FACING) == facing) {
                    visitedWithDepth.put(nextPos, depth + 1);
                    queue.add(nextPos);
                }
            }
        }

        for (BlockPos entryPos : fullGroup) {
            BlockState state = world.getBlockState(entryPos);
            if (State.isExposed(state) == groupPowered) continue;
            if (world.getBlockTickScheduler().isQueued(entryPos, this)) continue;
            if (groupPowered) {
                world.scheduleBlockTick(entryPos, this, visitedWithDepth.get(entryPos) * GROUP_EXPOSE_PROPAGATION_SPEED);
            } else {
                world.scheduleBlockTick(entryPos, this, visitedWithDepth.get(entryPos) * GROUP_RETRACT_PROPAGATION_SPEED);
            }
        }
    }

    private static HashSet<Direction> perpendicularDirections(Direction facing) {
        HashSet<Direction> result = new HashSet<>();
        for (Direction entry : Direction.values()) {
            if (entry.getAxis() == facing.getAxis()) continue;
            result.add(entry);
        }
        return result;
    }

    @SuppressWarnings("Convert2MethodRef")
    public enum State implements StringIdentifiable {
        DEFAULT(block -> ModelIds.getBlockModelId(block)),
        EXPOSED(block -> ModelIds.getBlockModelId(block).withSuffixedPath("_exposed")),
        EXPOSED_WITH_POTION(block -> ModelIds.getBlockModelId(block).withSuffixedPath("_exposed_potion"));

        private final Function<Block, Identifier> modelId;

        State(Function<Block, Identifier> modelId) {
            this.modelId = modelId;
        }

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public Identifier getModelId(Block block) {
            return modelId.apply(block);
        }

        public static EnumMap<State, Identifier> getModelIdMapping(Block block) {
            EnumMap<State, Identifier> result = new EnumMap<>(State.class);
            for (State state : State.values()) {
                result.put(state, state.getModelId(block));
            }
            return result;
        }

        public static boolean isExposed(BlockState state) {
            if (!state.contains(STATE)) return false;
            return state.get(STATE) == EXPOSED || state.get(STATE) == EXPOSED_WITH_POTION;
        }

        public static State getExposedState(World world, BlockPos pos) {
            if (!(world.getBlockEntity(pos) instanceof SpikeTrapBlockEntity blockEntity)) return EXPOSED;
            return blockEntity.hasPotion() ? EXPOSED_WITH_POTION : EXPOSED;
        }

        @SuppressWarnings("UnusedReturnValue")
        public static BlockState cycle(World world, BlockPos pos, boolean setBlockState) {
            BlockState state = world.getBlockState(pos);
            if (!state.contains(STATE)) return state;
            if (state.get(STATE) == DEFAULT) {
                state = state.with(STATE, getExposedState(world, pos));
            } else {
                state = state.with(STATE, DEFAULT);
            }
            if (setBlockState) {
                world.setBlockState(pos, state, NOTIFY_LISTENERS);
            }
            return state;
        }
    }
}
