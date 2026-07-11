package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.enchantment.MultiTargetBaseEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyExpressionValue(method = "getPossibleEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentTarget;isAcceptableItem(Lnet/minecraft/item/Item;)Z"))
    private static boolean isAcceptableItemWithMultiTargetCheck(boolean original, @Local(argsOnly = true) ItemStack stack, @Local Enchantment enchantment) {
        if (original) return true;
        if (!(enchantment instanceof MultiTargetBaseEnchantment multiTargetBaseEnchantment)) return false;
        return multiTargetBaseEnchantment.isAcceptableItem(stack);
    }
}
