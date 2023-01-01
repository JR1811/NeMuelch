package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    // https://github.com/LlamaLad7/MixinExtras
    @ModifyExpressionValue(method = "initialize", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/mob/ZombieEntity$ZombieData;tryChickenJockey:Z"))
    private boolean nemuelch$checkJockeySpawnWithConfig(boolean original) {
        return original && !ConfigInit.CONFIG.blockJockeySpawn;
    }
}
