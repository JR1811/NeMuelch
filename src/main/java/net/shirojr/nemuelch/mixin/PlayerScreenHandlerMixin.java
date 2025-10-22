package net.shirojr.nemuelch.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.init.NeMuelchEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/PlayerScreenHandler$1")
public class PlayerScreenHandlerMixin {
    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void preventEnchantedInsertions(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty() && EnchantmentHelper.getLevel(NeMuelchEnchantments.CURSE_OF_THE_BARE, stack) > 0) {
            cir.setReturnValue(false);
        }
    }
}
