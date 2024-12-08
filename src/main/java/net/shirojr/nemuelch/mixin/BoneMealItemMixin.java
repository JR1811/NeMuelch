package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.custom.supportItem.WateringCanItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @WrapOperation(method = "useOnGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private static void avoidWateringCanStackDecrement(ItemStack instance, int amount, Operation<Void> original) {
        if (!(instance.getItem() instanceof WateringCanItem)) {
            original.call(instance, amount);
        }
    }
}
