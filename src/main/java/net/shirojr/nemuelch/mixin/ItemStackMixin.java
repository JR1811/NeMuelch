package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.item.custom.supportItem.SoapItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isSectionVisible(ILnet/minecraft/item/ItemStack$TooltipSection;)Z", ordinal = 1))
    private void addCustomTooltips(@Nullable PlayerEntity player, TooltipContext context,
                                   CallbackInfoReturnable<List<Text>> cir, @Local(ordinal = 0) List<Text> list) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!SoapItem.hasCoating(stack)) return;
        if (SoapItem.hasInfiniteCoating(stack)) {
            list.add(Text.translatable("item.nemuelch.soap_coating_infinite"));
        } else {
            list.add(Text.translatable("item.nemuelch.soap_coating", SoapItem.getCoatingCharges(stack)));
        }
    }
}
