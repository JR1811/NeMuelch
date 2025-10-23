package net.shirojr.nemuelch.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void checkNbtForGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(ItemStack.DISPLAY_KEY)) return;
        NbtCompound displayNbt = nbt.getCompound(ItemStack.DISPLAY_KEY);
        if (!displayNbt.contains("glint")) return;
        cir.setReturnValue(displayNbt.getBoolean("glint"));
    }
}
