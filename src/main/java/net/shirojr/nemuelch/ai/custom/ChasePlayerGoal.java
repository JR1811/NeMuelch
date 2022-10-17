package net.shirojr.nemuelch.ai.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ChasePlayerGoal extends Goal {

    @Nullable
    private LivingEntity target;
    private final OnionEntity onion;
    double chaseDistance = 256.0D;

    public ChasePlayerGoal(OnionEntity onion) {
        this.onion = onion;
        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    public boolean canStart() {

        this.target = this.onion.getTarget();

        if (!(this.target instanceof PlayerEntity)) {
            return false;

        } else {

            double distanceToTarget = this.target.squaredDistanceTo(this.onion);
            return !(distanceToTarget > chaseDistance) && !this.onion.isSummoner((PlayerEntity) this.target);
        }
    }

    public void start() {
        this.onion.getNavigation().stop();
    }

    public void tick() {
        this.onion.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
    }
}
