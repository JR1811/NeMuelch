package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.util.RestrictedRendering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At(value = "TAIL"))
    private void onStartTrackingIllusions(ServerPlayerEntity player, CallbackInfo ci) {
        if (!(entity instanceof RestrictedRendering restrictedRendering)) return;
        restrictedRendering.nemuelch$updateTrackedEntityIds(player.getWorld());
    }
}
