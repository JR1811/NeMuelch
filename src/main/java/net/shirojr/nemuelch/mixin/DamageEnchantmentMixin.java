package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
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
                if (NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getSharpness() == 5) return;
                info.setReturnValue(NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getSharpness());
            }
            // smite
            case 1 -> {
                if (NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getSmite() == 5) return;
                info.setReturnValue(NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getSmite());
            }
            // bane_of_arthropods
            case 2 -> {
                if (NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getBane() == 5) return;
                info.setReturnValue(NeMuelchConfigInit.CONFIG.enchantmentLevelCap.getBane());
            }
        }
    }
}