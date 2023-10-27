package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageEnchantment.class)
public abstract class DamageEnchantmentMixin extends Enchantment {
    @Shadow
    @Final
    public int typeIndex;

    protected DamageEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nemuelch$getMaxLevelCapDamage(CallbackInfoReturnable<Integer> info) {
        switch (typeIndex) {
            // sharpness
            case 0 -> {
                if (ConfigInit.CONFIG.sharpnessEnchantmentLevelCap == 5) return;
                info.setReturnValue(ConfigInit.CONFIG.sharpnessEnchantmentLevelCap);
            }
            // smite
            case 1 -> {
                if (ConfigInit.CONFIG.smiteEnchantmentLevelCap == 5) return;
                info.setReturnValue(ConfigInit.CONFIG.smiteEnchantmentLevelCap);
            }
            // bane_of_arthropods
            case 2 -> {
                if (ConfigInit.CONFIG.baneEnchantmentLevelCap == 5) return;
                info.setReturnValue(ConfigInit.CONFIG.baneEnchantmentLevelCap);
            }
        }
    }
}