package net.shirojr.nemuelch.ai.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class OnionIgniteGoal extends Goal {

    private final OnionEntity onion;

    @Nullable
    private LivingEntity target;


    public OnionIgniteGoal(OnionEntity onion) {

        this.onion = onion;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity onionTarget = this.onion.getTarget();
        if (onionTarget == null) return false;
        if (onionTarget.getClass() == OnionEntity.class) return false;
        return this.onion.getFuseSpeed() > 0 || this.onion.squaredDistanceTo(onionTarget) < 5.0;
    }

    @Override
    public void start() {
        this.onion.getNavigation().stop();
        this.target = this.onion.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            this.onion.setFuseSpeed(-1);
            return;
        }
        if (this.onion.squaredDistanceTo(this.target) > 49.0) {
            this.onion.setFuseSpeed(-1);
            return;
        }
        if (!this.onion.getVisibilityCache().canSee(this.target)) {
            this.onion.setFuseSpeed(-1);
            return;
        }
        this.onion.setFuseSpeed(1);
    }
}
