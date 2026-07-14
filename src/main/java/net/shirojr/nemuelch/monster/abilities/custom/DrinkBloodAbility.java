package net.shirojr.nemuelch.monster.abilities.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;
import net.shirojr.nemuelch.util.helper.RaycastHelper;

import java.util.Optional;

public class DrinkBloodAbility extends ActiveAbility {
    private final BloodDrinker callback;

    private double reach;

    public DrinkBloodAbility(BloodDrinker callback, int cooldown, double reach) {
        super(cooldown);
        this.callback = callback;
        this.reach = reach;
    }

    public double getReach() {
        return reach;
    }

    public void setReach(double reach) {
        this.reach = reach;
    }

    @Override
    public void keybindInteraction(int index, ServerPlayerEntity user, boolean pressed) {
        super.keybindInteraction(index, user, pressed);
        if (isOnCooldown() || this.reach <= 0) return;
        Vec3d reachVec = user.getRotationVec(1f).multiply(this.reach);
        Vec3d start = user.getEyePos();
        Vec3d end = start.add(reachVec);
        Optional<EntityHitResult> entityHitResult = RaycastHelper.raycastEntities(
                user, start, end,
                entity -> entity instanceof AnimalEntity || entity.getType().isIn(NeMuelchTags.EntityTypes.HAS_BLOOD),
                true
        );
        if (entityHitResult.isEmpty()) return;
        if (!(entityHitResult.get().getEntity() instanceof LivingEntity livingEntityTarget)) return;
        this.callback.onDrankBlood(user, livingEntityTarget);
        startCooldown();
    }

    public interface BloodDrinker {
        long getConsumedBlood();

        void setConsumedBlood(long consumedBlood);

        void addConsumedBlood(long consumedBlood);

        long getBloodIntakeCapacity();

        void onDrankBlood(ServerPlayerEntity user, LivingEntity target);
    }
}
