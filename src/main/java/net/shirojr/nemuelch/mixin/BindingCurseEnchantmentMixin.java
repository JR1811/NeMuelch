package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.BindingCurseEnchantment;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.custom.weaponry.NeMuelchShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BindingCurseEnchantment.class)
public abstract class BindingCurseEnchantmentMixin {
    @ModifyExpressionValue(method = "isAcceptableItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean isCustomShield(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || NeMuelchShieldItem.isShieldItem(stack);
    }
}
