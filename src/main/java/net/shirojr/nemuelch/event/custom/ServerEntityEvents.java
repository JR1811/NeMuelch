package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.occasion.OccasionEntry;

public class ServerEntityEvents implements ServerEntityCombatEvents.AfterKilledOtherEntity {
    @Override
    public void afterKilledOtherEntity(ServerWorld world, Entity entity, LivingEntity killedEntity) {
        OccasionsWorldComponent worldComponent = OccasionsWorldComponent.get(world);
        for (OccasionEntry entry : worldComponent.getUnsyncedActiveOccasions()) {
            entry.getType().afterEntityKill(world, entity, killedEntity);
        }
    }
}
