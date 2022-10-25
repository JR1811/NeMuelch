package net.shirojr.nemuelch.ai.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class ChaseAllButSummonerGoal<T extends LivingEntity> extends TrackTargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;
    protected final int reciprocalChance;
    @Nullable
    protected LivingEntity summoner;
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;

    public ChaseAllButSummonerGoal(MobEntity mob, Class<T> targetClass, LivingEntity summoner, boolean checkVisibility) {
        this(mob, targetClass, summoner, DEFAULT_RECIPROCAL_CHANCE, checkVisibility, false, null);
    }

    public ChaseAllButSummonerGoal(MobEntity mob, Class<T> targetClass, LivingEntity summoner, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, checkVisibility, checkCanNavigate);
        this.targetClass = targetClass;
        this.summoner = summoner;
        this.reciprocalChance = toGoalTicks(reciprocalChance);
        this.setControls(EnumSet.of(Control.TARGET));
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
    }

    @Override
    public boolean canStart() {
        if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
            return false;
        } else {
            this.findClosestTarget();
            return this.targetEntity != null;
        }
    }

    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }

    protected Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0, distance);
    }

    protected void findClosestTarget() {
        LivingEntity closestPlayer = this.targetEntity = this.getClosestPlayerTranslator(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        LivingEntity closestEntity = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), (livingEntity) -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());


        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
            this.targetEntity = closestEntity;
        } else if (closestPlayer != this.summoner) {
            this.targetEntity = closestPlayer;
        }
    }

    //FIXME: clean this sh*t up since that's just an unnecessary step
    private LivingEntity getClosestPlayerTranslator(TargetPredicate targetPredicate, MobEntity mob, double x, double y, double z) {
        return this.getClosestEntityWithoutSummoner(this.mob.world.getPlayers(), targetPredicate, summoner, x, y, z);
    }

    @Nullable LivingEntity getClosestEntityWithoutSummoner(List<? extends PlayerEntity> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
        double d = -1.0;
        LivingEntity livingEntity = null;
        Iterator<? extends PlayerEntity> entityIterator = entityList.iterator();

        while (true) {
            LivingEntity livingEntity2;
            double sqDistance;
            do {
                do {
                    if (!entityIterator.hasNext()) {
                        return livingEntity;
                    }

                    livingEntity2 = entityIterator.next();
                } while (!targetPredicate.test(entity, livingEntity2));

                sqDistance = livingEntity2.squaredDistanceTo(x, y, z);
            } while (d != -1.0 && !(sqDistance < d));

            d = sqDistance;
            livingEntity = livingEntity2;
        }
    }
}
