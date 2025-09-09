package net.shirojr.nemuelch.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.shirojr.nemuelch.compat.statement.StatementCompat;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Debug(export = true)
@Mixin(ShovelItem.class)
public abstract class ShovelItemMixin extends MiningToolItem {
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
}
