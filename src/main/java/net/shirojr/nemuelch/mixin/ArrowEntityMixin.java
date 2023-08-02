package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin
        extends PersistentProjectileEntity
        implements Fertilizable {

    protected ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onHit", at = @At(value = "TAIL"))
    private void getAvailableMoisture(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity player) {
            if (player.getWorld().isClient() || !player.isFallFlying() ||
                    player.getHealth() > player.getMaxHealth() / 2) return;
            player.stopFallFlying();
        }
    }
}
