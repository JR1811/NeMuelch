package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;

public class SleepEvents implements EntitySleepEvents.StartSleeping, EntitySleepEvents.StopSleeping {

    @Override
    public void onStartSleeping(LivingEntity livingEntity, BlockPos blockPos) {
        MonsterComponent.get(livingEntity).getAbilities().onStartSleeping(blockPos);
    }

    @Override
    public void onStopSleeping(LivingEntity livingEntity, BlockPos blockPos) {
        MonsterComponent.get(livingEntity).getAbilities().onStopSleeping(blockPos);
    }
}
