package net.shirojr.nemuelch.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.State;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.statement.StatementCompat;
import net.shirojr.nemuelch.datapack.RandomTickSpeedChanceDatapack;
import net.shirojr.nemuelch.event.custom.BlockCallbacks;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Debug(export = true)
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin extends State<Block, BlockState> {
    private AbstractBlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<BlockState> codec) {
        super(owner, entries, codec);
    }

    @Shadow
    protected abstract BlockState asBlockState();

    @Inject(method = "scheduledTick", at = @At("HEAD"))
    private void scheduleSandPathTick(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        if (world.getBlockState(pos.up()).isAir()) return;
        StatementCompat.setToSand(null, state, world, pos);
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasSidedTransparency(Lnet/minecraft/block/BlockState;)Z"))
    private boolean setSandPathSidedTransparency(boolean original) {
        BlockState state = asBlockState();
        if (!state.isOf(Blocks.SAND)) return original;
        if (StatementCompat.isNotPath(state)) return original;
        return true;
    }

    @Inject(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void getSandPathOutlineShape(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = asBlockState();
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        cir.setReturnValue(StatementCompat.getPathShape());
    }

    @Inject(method = "getCullingShape", at = @At("HEAD"), cancellable = true)
    private void getSandPathCullingShape(BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = asBlockState();
        if (StatementCompat.isNotPath(state)) return;
        cir.setReturnValue(StatementCompat.getPathShape());
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    private void getSandPathStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = asBlockState();
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, state.getBlock(), 1);
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    private void canPlaceSandPathAt(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = asBlockState();
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        BlockState stateAbove = world.getBlockState(pos.up());
        cir.setReturnValue(!stateAbove.isSolid() || stateAbove.getBlock() instanceof FenceGateBlock);
    }

    @Inject(method = "canPathfindThrough", at = @At("HEAD"), cancellable = true)
    private void canPathFindThroughSandPath(BlockView world, BlockPos pos, NavigationType type, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = asBlockState();
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void randomTickWithDatapackChance(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!NeMuelchConfigInit.CONFIG.enableRandomTickChanceLimitFeature) return;
        Float chance = RandomTickSpeedChanceDatapack.BLOCK_CHANCES.get(asBlockState().getBlock());
        if (chance == null || chance >= 1) return;
        if (chance >= 0) {
            if (chance > world.getRandom().nextFloat()) return;
        }
        ci.cancel();
    }

    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void cleanseBlight(World world, BlockPos pos, BlockState state, boolean notify, CallbackInfo ci) {
        if (BlightChunkComponent.NO_BLIGHT.test(state)) return;
        boolean stateCanCleanse = false;
        if (state.contains(Properties.LIT) && state.get(Properties.LIT)) {
            if (state.getBlock() instanceof CampfireBlock) stateCanCleanse = true;
            if (state.getBlock() instanceof AbstractFurnaceBlock) stateCanCleanse = true;
        }
        if (state.getBlock() instanceof LavaCauldronBlock || state.getFluidState().isIn(FluidTags.LAVA)) {
            stateCanCleanse = true;
        }

        if (!stateCanCleanse) return;

        BlightChunkComponent.maybeGet(world.getChunk(pos)).ifPresent(component -> {
            component.clearPos(pos, Set.of());
            for (Direction value : Direction.values()) {
                component.clearPos(pos.offset(value), Set.of());
            }
        });
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void onStateReplacedForBlight(World world, BlockPos pos, BlockState state, boolean moved, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        BlightChunkComponent.maybeGet(world.getChunk(pos)).ifPresent(component -> {
                    serverWorld.getProfiler().push("nemuelch_on_stepped_on_blight");
                    component.getBlightsOfPos(pos).forEach(type -> type.getActions().get().onBlockStateChanged(
                            serverWorld, component.getTimeOfFirstInitializedBlight(), pos, this.asBlockState(), state)
                    );
                    serverWorld.getProfiler().pop();
                }
        );
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"))
    private void onEntityCollidingWithBlight(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        BlightChunkComponent.maybeGet(serverWorld.getChunk(pos)).ifPresent(component -> {
            serverWorld.getProfiler().push("nemuelch_on_entity_collision_with_blight");
            component.getBlightsOfPos(pos).forEach(type -> type.getActions().get().onBlockCollision(
                    serverWorld, component.getTimeOfFirstInitializedBlight(), pos, entity
            ));
            serverWorld.getProfiler().pop();
        });
    }

    @WrapOperation(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"))
    private void wrapForBlockAddedCallback(Block instance, BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, Operation<Void> original) {
        BlockCallbacks.ON_ADDED.invoker().onBlockAdded(world, pos, state, oldState);
        original.call(instance, state, world, pos, oldState, notify);
    }
}
