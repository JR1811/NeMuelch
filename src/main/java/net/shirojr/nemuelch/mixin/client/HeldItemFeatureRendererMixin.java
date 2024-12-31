package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {
    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean renderPotItem(ItemStack instance, Operation<Boolean> original, @Local(argsOnly = true) LivingEntity entity) {
        if (instance.getItem() instanceof DropPotBlockItem && entity.isFallFlying()) {
            return true;     // avoid rendering in hand
        }
        return original.call(instance);
    }
}
