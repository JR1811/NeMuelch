package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Fertilizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin
        extends PersistentProjectileEntity
        implements Fertilizable {

    protected ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onHit", at = @At(value = "TAIL"))
    private void nemuelch$handleFlyingPlayerEntityHit(LivingEntity target, CallbackInfo ci) {
        if (!(target instanceof PlayerEntity player)) return;
        if (player.getWorld().isClient() || !player.isFallFlying() ||
                player.getHealth() > player.getMaxHealth() / 2) return;

        player.stopFallFlying();
    }

}
