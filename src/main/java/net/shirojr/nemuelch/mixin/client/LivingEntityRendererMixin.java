package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.compat.cca.implementation.CombEntityComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @ModifyExpressionValue(method = "isShaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFrozen()Z"))
    private boolean isFrozenOrCombed(boolean original, @Local(argsOnly = true) LivingEntity entity) {
        CombEntityComponent component = CombEntityComponent.get(entity);
        return original ||component.isActiveCombing();
    }
}
