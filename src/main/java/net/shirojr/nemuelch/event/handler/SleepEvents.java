package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.component.GeneralMonsterComponent;
import net.shirojr.nemuelch.monster.AbstractMonsterType;

public class SleepEvents implements EntitySleepEvents.StartSleeping, EntitySleepEvents.StopSleeping {

    @Override
    public void onStartSleeping(LivingEntity livingEntity, BlockPos blockPos) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(livingEntity);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onStartSleeping(blockPos);
        }
    }

    @Override
    public void onStopSleeping(LivingEntity livingEntity, BlockPos blockPos) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(livingEntity);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onStopSleeping(blockPos);
        }
    }
}
