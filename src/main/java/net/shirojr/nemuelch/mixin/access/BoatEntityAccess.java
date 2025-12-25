package net.shirojr.nemuelch.mixin.access;

import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BoatEntity.class)
public interface BoatEntityAccess {
    @Invoker("checkLocation")
    BoatEntity.Location neMuelch$checkLocation();
}
