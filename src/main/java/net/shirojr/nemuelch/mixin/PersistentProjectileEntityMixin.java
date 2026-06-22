package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.shirojr.nemuelch.item.custom.weaponry.NeMuelchShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    private void replaceReflectLogic(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (!(entityHitResult.getEntity() instanceof LivingEntity target)) return;
        if (target.getActiveItem().getItem() instanceof NeMuelchShieldItem shieldItem) {
            if (shieldItem.isBlocking(target)) {
                shieldItem.onBlockingPersistentProjectile(target, (PersistentProjectileEntity) (Object) this);
                ci.cancel();
            }
        }
    }
}
