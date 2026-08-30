package net.shirojr.nemuelch.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.ComplexZone;
import net.shirojr.nemuelch.event.custom.NotificationZoneCallbacks;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityNotificationZoneMixin extends LivingEntity {
    @Unique
    private HashSet<ComplexZone> previouslyInside = new HashSet<>();
    @Unique
    private BlockPos lastCheckedPos = null;

    private PlayerEntityNotificationZoneMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void checkForEdgeStates(CallbackInfo ci) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        int tickSpeed = serverWorld.getGameRules().getInt(NemuelchGameRules.NOTIFICATION_ZONE_TICK_SPEED);
        if (tickSpeed == 0 || this.age % tickSpeed != 0) return;
        if (this.getBlockPos().equals(this.lastCheckedPos)) return;
        this.lastCheckedPos = this.getBlockPos();

        NotificationZoneComponent component = NotificationZoneComponent.get(getWorld());

        HashSet<ComplexZone> newZones = component.getContainingZones(this.getBlockPos());
        if (this.previouslyInside.equals(newZones)) return;

        HashSet<ComplexZone> entered = new HashSet<>(newZones);
        entered.removeAll(this.previouslyInside);
        entered.forEach(zone -> NotificationZoneCallbacks.ENTERED_ZONE.invoker().onZoneEntered(component, zone, this));

        HashSet<ComplexZone> left = new HashSet<>(this.previouslyInside);
        left.removeAll(newZones);
        left.forEach(zone -> NotificationZoneCallbacks.LEFT_ZONE.invoker().onZoneLeft(component, zone, this));

        this.previouslyInside.clear();
        this.previouslyInside.addAll(newZones);
    }
}
