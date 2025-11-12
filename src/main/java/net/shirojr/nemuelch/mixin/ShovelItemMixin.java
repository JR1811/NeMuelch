package net.shirojr.nemuelch.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.statement.StatementCompat;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Debug(export = true)
@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin extends MiningToolItem {
    @Shadow
    @Final
    protected static Map<Block, BlockState> PATH_STATES;

    private ShovelItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;",
                    ordinal = 0,
                    remap = false
            )
    )
    private static ImmutableMap.Builder<Block, BlockState> addSandPathState(ImmutableMap.Builder<Block, BlockState> original) {
        if (StatementCompat.isStatementMissing()) return original;
        original.put(Blocks.SAND, StatementCompat.getStateWithPath(Blocks.SAND.getDefaultState(), true));
        return original;
    }

    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <V> V preventSandPathModification(Map<Block, BlockState> instance, Object o, Operation<V> original, @Local BlockState state) {
        if (StatementCompat.isNotPath(state)) return original.call(instance, o);
        return null;
    }

    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object forwardPathMakingDown(Map<Block, BlockState> instance, Object o, Operation<Object> original,
                                         @Local(argsOnly = true) ItemUsageContext context,
                                         @Local LocalRef<BlockPos> pos,
                                         @Local LocalRef<BlockState> state) {
        BlockState originalState = state.get();
        BlockPos originalPos = pos.get();
        BlockState originalPathState = PATH_STATES.get(originalState.getBlock());
        if (originalPathState != null) return originalPathState;
        if (!NeMuelchConfigInit.CONFIG.forwardPathMakingThroughReplacables) return null;
        World world = context.getWorld();
        BlockPos posBelow = originalPos.down();
        BlockState stateBelow = world.getBlockState(posBelow);
        BlockState pathStateBelow = PATH_STATES.get(stateBelow.getBlock());
        if (pathStateBelow == null) return null;
        if (!originalState.isReplaceable() && !originalState.isIn(NeMuelchTags.Blocks.IGNORED_BY_SHOVEL_FLATTENING)) return null;
        world.breakBlock(originalPos, true, context.getPlayer());
        pos.set(posBelow);
        state.set(stateBelow);
        return pathStateBelow;
    }
}
