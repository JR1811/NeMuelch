package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.onyxstudios.cca.api.v3.component.ComponentAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.duck.Restorable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, ComponentAccess {
    @Nullable
    @WrapOperation(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"
            )
    )
    private Explosion makeRefillable(World world, Entity entity, DamageSource damageSource, ExplosionBehavior behavior,
                                     double x, double y, double z, float power, boolean createFire,
                                     Explosion.DestructionType destructionType, Operation<Explosion> original) {
        Explosion originalCall = original.call(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
        if (entity != null && entity.getType().isIn(NeMuelchTags.EntityTypes.EXPLOSIONS_REFILL) && originalCall instanceof Restorable restorable) {
            restorable.nemuelch$setRestorable();
        }
        return originalCall;
    }
}
