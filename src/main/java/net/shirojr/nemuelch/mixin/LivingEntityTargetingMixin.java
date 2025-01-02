package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.util.Illusionable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityTargetingMixin {
    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void avoidTargetingIllusions(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Illusionable illusionSelf && illusionSelf.nemuelch$isIllusion()) {
            if (!illusionSelf.nemuelch$getIllusionTargets().contains(target)) {
                cir.setReturnValue(false);
                return;
            }
        }
        if (target instanceof Illusionable illusionTarget && illusionTarget.nemuelch$isIllusion()) {
            if (!illusionTarget.nemuelch$getIllusionTargets().contains(self)) {
                cir.setReturnValue(false);
            }
        }
    }
}
