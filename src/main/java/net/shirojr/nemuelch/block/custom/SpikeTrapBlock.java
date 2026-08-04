package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
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
import net.shirojr.nemuelch.block.util.VoxelShapeUtil;
import net.shirojr.nemuelch.init.NeMuelchDamageTypes;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class SpikeTrapBlock extends Block implements Waterloggable {
    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty EXPOSED = NeMuelchProperties.EXPOSED;

    private static final int EXPOSED_RETRACT_DELAY = 25;
    private static final int MAX_GROUP_SIZE = 512;

    private static final Function<BlockState, VoxelShape> SMALL_SHAPE = state ->
            VoxelShapeUtil.createRotatedShape(new int[]{0, 0, 0, 16, 2, 16}, state.get(FACING));

    public SpikeTrapBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(FACING, Direction.UP)
                .with(WATERLOGGED, false)
                .with(EXPOSED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, WATERLOGGED, EXPOSED);
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
        if (state.get(EXPOSED)) {
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
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState()
                .with(FACING, direction)
                .with(WATERLOGGED, fluidState.getFluid().equals(Fluids.WATER))
                .with(EXPOSED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
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
        return !state.get(EXPOSED);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (!state.get(EXPOSED)) return;
        livingEntity.slowMovement(state, new Vec3d(0.6F, 0.1F, 0.6F));
        if (world instanceof ServerWorld serverWorld) {
            if (livingEntity.lastRenderX != livingEntity.getX() || livingEntity.lastRenderY != livingEntity.getY() || livingEntity.lastRenderZ != livingEntity.getZ()) {
                double xDifference = Math.abs(livingEntity.getX() - livingEntity.lastRenderX);
                double yDifference = Math.abs(livingEntity.getY() - livingEntity.lastRenderY);
                double zDifference = Math.abs(livingEntity.getZ() - livingEntity.lastRenderZ);
                if (xDifference >= 0.003F || yDifference > 0F || zDifference >= 0.003F) {
                    livingEntity.damage(NeMuelchDamageTypes.of(serverWorld, NeMuelchDamageTypes.PIERCING), 2.0F);
                    if (!livingEntity.hasStatusEffect(StatusEffects.WEAKNESS)) {
                        livingEntity.addStatusEffect(
                                new StatusEffectInstance(StatusEffects.WEAKNESS, 50, 1,
                                        false, false, true)
                        );
                    }
                }
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (!(world instanceof ServerWorld serverWorld)) return;
        boolean isExposed = state.get(EXPOSED);
        if (isExposed != serverWorld.isReceivingRedstonePower(pos)) {
            if (isExposed) {
                serverWorld.scheduleBlockTick(pos, this, EXPOSED_RETRACT_DELAY);
                serverWorld.playSound(null, pos, NeMuelchSounds.SPIKE_TRAP_RETRACT, SoundCategory.BLOCKS, 3, 1);
            } else {
                serverWorld.setBlockState(pos, state.cycle(EXPOSED), Block.NOTIFY_LISTENERS);
                serverWorld.playSound(null, pos, NeMuelchSounds.SPIKE_TRAP_EXPOSE, SoundCategory.BLOCKS, 3, 1);
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        if (state.get(EXPOSED) && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(EXPOSED), Block.NOTIFY_LISTENERS);
        }
    }

    public ActionResult onAttackBlock(LivingEntity attacker, BlockState state) {
        if (attacker instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) {
            return ActionResult.PASS;
        }
        if (state.contains(EXPOSED) && state.get(EXPOSED)) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private boolean isGroupPowered(ServerWorld world, BlockPos originPos, Direction facing) {
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

    private static HashSet<Direction> perpendicularDirections(Direction facing) {
        HashSet<Direction> result = new HashSet<>();
        for (Direction entry : Direction.values()) {
            if (entry.getAxis() == facing.getAxis()) continue;
            result.add(entry);
        }
        return result;
    }
}
