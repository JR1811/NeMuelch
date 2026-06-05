package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import net.shirojr.nemuelch.util.duck.MobPersistency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter, MobPersistency {
    @Shadow
    private boolean persistent;

    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean nemuelch$isPersistent() {
        return persistent;
    }

    @Override
    public void nemuelch$setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
}
