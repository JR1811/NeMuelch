package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtectionEnchantment.class)
public abstract class ProtectionEnchantmentMixin extends Enchantment {
    protected ProtectionEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD")/*, remap = false*/, cancellable = true)
    private void nemuelch$getMaxLevelCap(CallbackInfoReturnable<Integer> info) {
        // test for vanilla value
        if(ConfigInit.CONFIG.protectionEnchantmentLevelCap != 4) {
            info.setReturnValue(ConfigInit.CONFIG.protectionEnchantmentLevelCap);
        }
    }
}