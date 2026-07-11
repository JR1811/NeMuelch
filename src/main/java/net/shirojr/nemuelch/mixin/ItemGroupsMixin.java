package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.ItemGroups;
import net.shirojr.nemuelch.enchantment.MultiTargetBaseEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(ItemGroups.class)
public abstract class ItemGroupsMixin {
    @ModifyExpressionValue(
            method = "method_48951(Ljava/util/Set;Lnet/minecraft/enchantment/Enchantment;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private static boolean addMaxLevelEnchantedBooksIncludingMultiTarget(boolean original,
                                                                         @Local(argsOnly = true) Set<EnchantmentTarget> targets,
                                                                         @Local(argsOnly = true) Enchantment enchantment) {
        return original || containedInMultiTarget(enchantment, targets);
    }

    @ModifyExpressionValue(
            method = "method_48946(Ljava/util/Set;Lnet/minecraft/enchantment/Enchantment;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private static boolean addAllLevelEnchantedBooksIncludingMultiTarget(boolean original,
                                                                         @Local(argsOnly = true) Set<EnchantmentTarget> targets,
                                                                         @Local(argsOnly = true) Enchantment enchantment) {
        return original || containedInMultiTarget(enchantment, targets);
    }

    @Unique
    private static boolean containedInMultiTarget(Enchantment enchantment, Set<EnchantmentTarget> targets) {
        if (!(enchantment instanceof MultiTargetBaseEnchantment multiTargetBaseEnchantment)) return false;
        for (EnchantmentTarget target : targets) {
            if (multiTargetBaseEnchantment.getEnchantmentTargets().contains(target)) {
                return true;
            }
        }
        return false;
    }
}
