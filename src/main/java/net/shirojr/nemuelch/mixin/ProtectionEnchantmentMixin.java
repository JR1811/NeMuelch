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
                if (ConfigInit.CONFIG.protectionEnchantmentLevelCap == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.protectionEnchantmentLevelCap);
            }
            case FALL -> {
                if (ConfigInit.CONFIG.fallProtectionEnchantmentLevelCap == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.fallProtectionEnchantmentLevelCap);
            }
            case FIRE -> {
                if (ConfigInit.CONFIG.fireProtectionEnchantmentLevelCap == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.fireProtectionEnchantmentLevelCap);
            }
            case EXPLOSION -> {
                if (ConfigInit.CONFIG.explosionProtectionEnchantmentLevelCap == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.explosionProtectionEnchantmentLevelCap);
            }
            case PROJECTILE -> {
                if (ConfigInit.CONFIG.projectileProtectionEnchantmentLevelCap == 4) return;
                cir.setReturnValue(ConfigInit.CONFIG.projectileProtectionEnchantmentLevelCap);
            }
        }
    }
}