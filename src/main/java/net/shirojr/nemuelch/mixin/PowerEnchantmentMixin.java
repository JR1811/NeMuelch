package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.PowerEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowerEnchantment.class)
public abstract class PowerEnchantmentMixin extends Enchantment {
    protected PowerEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nemuelch$getMaxLevelCapPower(CallbackInfoReturnable<Integer> cir) {
        if (ConfigInit.CONFIG.enchantmentLevelCap.getPower() == 5) return;
        cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getPower());
    }
}
