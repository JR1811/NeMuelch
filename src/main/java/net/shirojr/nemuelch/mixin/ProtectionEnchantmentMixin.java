package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.shirojr.nemuelch.config.datatype.EnchantmentLevelData;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProtectionEnchantment.class)
public abstract class ProtectionEnchantmentMixin extends Enchantment {
    @Shadow
    @Final
    public ProtectionEnchantment.Type protectionType;

    protected ProtectionEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }


    @ModifyReturnValue(method = "getMaxLevel", at = @At("RETURN"))
    private int nemuelch$getMaxLevelCap(int original) {
        // This method will prevent hotswapping in general!
        // I have no idea why, but the make-shift fix is to
        // just don't do it in a dev environment...

        // after the help of fabric discord this seems to be a bigger problem?
        // java agent has been set in the run config too...
        //TODO: ask Modmuss or LLama for help
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return original;
        }
        EnchantmentLevelData enchantmentLevelData = NeMuelchConfigInit.CONFIG.enchantmentLevelCap;
        return switch (protectionType) {
            case ALL -> enchantmentLevelData.getProtection();
            case FALL -> enchantmentLevelData.getFallProtection();
            case FIRE -> enchantmentLevelData.getFireProtection();
            case EXPLOSION -> enchantmentLevelData.getExplosionProtection();
            case PROJECTILE -> enchantmentLevelData.getProjectileProtection();
        };
    }
}