package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.compat.statement.StatementCompat;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
    @Inject(method = "scheduledTick", at = @At("HEAD"))
    private void scheduleSandPathTick(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        StatementCompat.setToSand(null, state, world, pos);
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;hasSidedTransparency(Lnet/minecraft/block/BlockState;)Z"))
    private boolean setSandPathSidedTransparency(boolean original) {
        AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;
        if (!state.isOf(Blocks.SAND)) return original;
        if (StatementCompat.isNotPath(state)) return original;
        return true;
    }

    @Inject(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void setSandPathOutlineShape(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        cir.setReturnValue(Block.createCuboidShape(0, 0, 0, 16, 15, 16));
    }

    @Inject(method = "canPathfindThrough", at = @At("HEAD"), cancellable = true)
    private void canPathfindThroughSandPath(BlockView world, BlockPos pos, NavigationType type, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    private void getSandPathStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, state.getBlock(), 1);
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    private void canPlaceSandPathAt(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;
        if (!state.isOf(Blocks.SAND)) return;
        if (StatementCompat.isNotPath(state)) return;
        BlockState stateAbove = world.getBlockState(pos.up());
        cir.setReturnValue(!stateAbove.isSolid() || stateAbove.getBlock() instanceof FenceGateBlock);
    }
}
