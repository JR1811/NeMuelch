package net.shirojr.nemuelch.mixin;

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

@Mixin(ProtectionEnchantment.class)
public abstract class ProtectionEnchantmentMixin extends Enchantment {
    @Shadow @Final public ProtectionEnchantment.Type protectionType;

    protected ProtectionEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void nemuelch$getMaxLevelCap(CallbackInfoReturnable<Integer> cir) {
        switch (protectionType) {
            case ALL -> {
                if (ConfigInit.CONFIG.enchantmentLevelCap.getProtection() == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getProtection());
            }
            case FALL -> {
                if (ConfigInit.CONFIG.enchantmentLevelCap.getFallProtection() == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getFallProtection());
            }
            case FIRE -> {
                if (ConfigInit.CONFIG.enchantmentLevelCap.getFireProtection() == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getFireProtection());
            }
            case EXPLOSION -> {
                if (ConfigInit.CONFIG.enchantmentLevelCap.getExplosionProtection() == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getExplosionProtection());
            }
            case PROJECTILE -> {
                if (ConfigInit.CONFIG.enchantmentLevelCap.getProjectileProtection() == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.enchantmentLevelCap.getProjectileProtection());
            }
        }
    }
}