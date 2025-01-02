package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.Illusionable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityIllusionMixin {
    @Shadow
    public World world;

    @WrapOperation(method = "spawnSprintingParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"))
    private void avoidSprintingParticlesForIllusion(World instance, ParticleEffect parameters,
                                                    double x, double y, double z,
                                                    double velocityX, double velocityY, double velocityZ,
                                                    Operation<Void> original) {
        if (!world.isClient()) {
            original.call(instance, parameters, x, y, z, velocityX, velocityY, velocityZ);
            return;
        }
        // MinecraftClient client = MinecraftClient.getInstance();
        Entity entity = (Entity) (Object) this;
        boolean isIllusion = entity instanceof Illusionable restrictedRendering && restrictedRendering.nemuelch$isIllusion();
        // boolean visibleParticles = /*client.player != null &&*/ NeMuelchClient.ILLUSIONS_CACHE.contains(entity);

        if (!isIllusion /*|| visibleParticles*/) {
            original.call(instance, parameters, x, y, z, velocityX, velocityY, velocityZ);
        }
    }
}
