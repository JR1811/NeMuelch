package net.shirojr.nemuelch.effect.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.effect.util.StatusEffectLifecycle;

public class DeferredInstantEffect extends StatusEffect implements StatusEffectLifecycle {
    private final InstantStatusEffect afterFinish;

    public DeferredInstantEffect(StatusEffectCategory category, InstantStatusEffect afterFinish, int color) {
        super(category, color);
        this.afterFinish = afterFinish;
    }

    public InstantStatusEffect getAfterFinishEffect() {
        return afterFinish;
    }

    @Override
    public void onFinished(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
        entity.addStatusEffect(new StatusEffectInstance(getAfterFinishEffect(), 1, instance.getAmplifier()));
    }
}
