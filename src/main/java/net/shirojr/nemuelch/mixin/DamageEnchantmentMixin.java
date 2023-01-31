package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin extends Enchantment {
    @Shadow @Final public int typeIndex;

    protected DamageEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nemuelch$getMaxLevelCap(CallbackInfoReturnable<Integer> info) {
        switch (typeIndex) {
            // sharpness
            case 0:
                return;
            // smite
            case 1:
                if(ConfigInit.CONFIG.smiteEnchantmentLevelCap == 5) return;
                info.setReturnValue(ConfigInit.CONFIG.smiteEnchantmentLevelCap);
                break;
            // bane_of_arthropods
            case 2:
                return;
        }
    }
}