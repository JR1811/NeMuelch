package net.shirojr.nemuelch.monster.abilities.custom;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;
import net.shirojr.nemuelch.util.helper.RaycastHelper;

import java.util.Optional;

public class DrinkBloodAbility extends ActiveAbility {
    private final BloodDrinker callback;
    private final int keybindIndex;

    private double reach;

    public DrinkBloodAbility(BloodDrinker callback, int keybindIndex, int cooldown, double reach) {
        super(cooldown);
        this.callback = callback;
        this.keybindIndex = keybindIndex;
        this.reach = reach;
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

        void onDrankBlood(PlayerEntity user, LivingEntity target);
    }

    @SuppressWarnings("UnstableApiUsage")
    public enum BloodSource {
        ANIMAL(FluidConstants.DROPLET * 5, 5),
        MONSTER(FluidConstants.DROPLET * 7, 2),
        HUMANOID(FluidConstants.DROPLET * 20, 25),
        PLAYER(FluidConstants.DROPLET * 30, 40);

        private final long baseYield;
        private final int suspicion;

        BloodSource(long baseYield, int suspicion) {
            this.baseYield = baseYield;
            this.suspicion = suspicion;
        }

        public static Optional<BloodSource> get(LivingEntity target) {
            if (target instanceof AnimalEntity || target instanceof AmbientEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_ANIMAL)) {
                return Optional.of(ANIMAL);
            }
            if (target instanceof Monster || target instanceof SlimeEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_MONSTER)) {
                return Optional.of(MONSTER);
            }
            if (target instanceof PlayerEntity || target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_PLAYER)) {
                return Optional.of(PLAYER);
            }
            if (target.getType().isIn(NeMuelchTags.EntityTypes.MONSTER_FOOD_SOURCE_HUMANOID))
                return Optional.of(HUMANOID);
            return Optional.empty();
        }

        public long getBaseYield() {
            return baseYield;
        }

        public int getSuspicion() {
            return suspicion;
        }
    }
}
