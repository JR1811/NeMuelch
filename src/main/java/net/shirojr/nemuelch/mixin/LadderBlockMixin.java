package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(LadderBlock.class)
public abstract class LadderBlockMixin extends Block implements Waterloggable {
    private LadderBlockMixin(Settings settings) {
        super(settings);
    }

    @Definition(id = "getOpposite", method = "Lnet/minecraft/util/math/Direction;getOpposite()Lnet/minecraft/util/math/Direction;")
    @Definition(id = "get", method = "Lnet/minecraft/block/BlockState;get(Lnet/minecraft/state/property/Property;)Ljava/lang/Comparable;")
    @Expression("?.getOpposite() == ?.get(?)")
    @ModifyExpressionValue(method = "getStateForNeighborUpdate", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean expandValidUpdateDirections(boolean original, @Local(argsOnly = true) Direction direction) {
        if (!NeMuelchConfigInit.CONFIG.enableLadderFeatures) return original;
        return original || direction.getAxis().isVertical();
    }

    @WrapOperation(method = "canPlaceOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isSideSolidFullSquare(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean placeLadderModification(BlockState instance, BlockView blockView, BlockPos pos, Direction direction, Operation<Boolean> original) {
        boolean originalCall = original.call(instance, blockView, pos, direction);
        if (originalCall) return true;
        if (!NeMuelchConfigInit.CONFIG.enableLadderFeatures) return false;

        int maxLadderDepth = 256;
        List<BlockPos> connected = new ArrayList<>();
        int steps = 0;


        BlockPos.Mutable posWalker = pos.offset(direction).mutableCopy();
        while (steps <= maxLadderDepth && blockView.getBlockState(posWalker.move(Direction.UP)).isIn(NeMuelchTags.Blocks.CONVENTIONAL_LADDERS)) {
            steps++;
            BlockPos immutable = posWalker.toImmutable();
            connected.add(immutable);
            NeMuelch.LOGGER.info("UP: {} - {} - {}", pos, immutable, blockView.getBlockState(immutable));
        }
        steps = 0;
        posWalker.set(pos.offset(direction));
        while (steps <= maxLadderDepth && blockView.getBlockState(posWalker.move(Direction.DOWN)).isIn(NeMuelchTags.Blocks.CONVENTIONAL_LADDERS)) {
            steps++;
            BlockPos immutable = posWalker.toImmutable();
            connected.add(immutable);
            NeMuelch.LOGGER.info("DOWN: {} - {} - {}", pos, immutable, blockView.getBlockState(immutable));
        }
        boolean areLaddersSupported = false;
        for (BlockPos entryPos : connected) {
            if (isLadderSupported(blockView, entryPos, direction)) {
                areLaddersSupported = true;
                break;
            }
        }

        return areLaddersSupported;
    }

    @Unique
    private static boolean isLadderSupported(BlockView world, BlockPos ladderPos, Direction side) {
        BlockState ladderState = world.getBlockState(ladderPos);
        Direction facing = ladderState.contains(LadderBlock.FACING) ? ladderState.get(LadderBlock.FACING) : side;
        BlockPos supportPos = ladderPos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, facing);
    }
}
