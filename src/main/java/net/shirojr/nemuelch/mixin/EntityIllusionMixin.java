package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.wrapper.Illusionable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityIllusionMixin {
    @Shadow
    public World world;

    @WrapOperation(method = "spawnSprintingParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private void avoidSprintingParticlesForIllusion(World instance, ParticleEffect parameters,
                                                    double x, double y, double z,
                                                    double velocityX, double velocityY, double velocityZ,
                                                    Operation<Void> original) {
        Entity entity = (Entity) (Object) this;
        boolean isIllusion = entity instanceof Illusionable restrictedRendering && restrictedRendering.nemuelch$isIllusion();
        if (!isIllusion) {
            original.call(instance, parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
    private void avoidStepSound(Entity instance, SoundEvent sound, float volume, float pitch, Operation<Void> original) {
        if (!(instance instanceof Illusionable illusion) || !illusion.nemuelch$isIllusion()) {
            original.call(instance, sound, volume, pitch);
        }
    }

    @Inject(method = "isSilent", at = @At("HEAD"), cancellable = true)
    private void avoidIllusionSound(CallbackInfoReturnable<Boolean> cir) {
        if (!((Entity) (Object) this instanceof Illusionable illusion)) return;
        if (illusion.nemuelch$isIllusion()) cir.setReturnValue(true);
    }
}
