package net.shirojr.nemuelch.ai.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ChaseAllButSummonerGoal<T extends LivingEntity> extends TrackTargetGoal {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;
    protected final Class<T> targetClass;
    protected final int reciprocalChance;
    @Nullable
    protected UUID summoner;
    protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;


    public ChaseAllButSummonerGoal(MobEntity mob, Class<T> targetClass, UUID summonerUUID, boolean checkVisibility) {
        this(mob, targetClass, summonerUUID, DEFAULT_RECIPROCAL_CHANCE, checkVisibility, false, null);
    }

    public ChaseAllButSummonerGoal(MobEntity mob, Class<T> targetClass, @Nullable UUID summoner, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
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
            return this.summoner != null && this.targetEntity != null;
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

        //List<? extends PlayerEntity> playerList = this.mob.world.getPlayers();
        List<? extends LivingEntity> playerList = this.mob.world.getEntitiesByType(TypeFilter.instanceOf(this.targetClass),
                this.getSearchBox(this.getFollowRange()),
                entity -> !entity.getUuid().equals(this.summoner));

        double x = this.mob.getX();
        double y = this.mob.getEyeY();
        double z = this.mob.getZ();
        Box searchBox = this.getSearchBox(this.getFollowRange());

        //LivingEntity closestPlayer = this.targetEntity = this.getClosestPlayerTranslator(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());

        LivingEntity closestPlayer = this.targetEntity = this.getClosestEntityWithoutSummoner(playerList, this.targetPredicate, this.mob, x, y, z);
        LivingEntity closestEntity = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetClass, searchBox, (livingEntity) -> true), this.targetPredicate, this.mob, x, y, z);

        if (this.targetClass != PlayerEntity.class &&
                this.targetClass != ServerPlayerEntity.class &&
                this.targetClass != OnionEntity.class) {

            this.targetEntity = closestEntity;

        } else if (closestPlayer != null && !closestPlayer.getUuid().equals(this.summoner)) {
            this.targetEntity = closestPlayer;
        }
    }

    //FIXME: Living entity as the summoner is being filtered already in findClosestTarget() !
    @Nullable LivingEntity getClosestEntityWithoutSummoner(List<? extends LivingEntity> entityList, TargetPredicate targetPredicate, @Nullable LivingEntity entity, double x, double y, double z) {
        double d = -1.0;
        LivingEntity livingEntity = null;
        Iterator<? extends LivingEntity> entityIterator = entityList.iterator();

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
