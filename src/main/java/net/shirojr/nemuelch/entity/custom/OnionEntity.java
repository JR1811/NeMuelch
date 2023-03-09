package net.shirojr.nemuelch.entity.custom;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.nemuelch.ai.custom.ChaseAllButSummonerGoal;
import net.shirojr.nemuelch.ai.custom.OnionIgniteGoal;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import org.lwjgl.system.CallbackI;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class OnionEntity extends HostileEntity implements IAnimatable {

    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(OnionEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(OnionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int explosionRadius = ConfigInit.CONFIG.onionEntityExplosionRadius;
    private final float effectRadius = ConfigInit.CONFIG.onionEntityEffectRadius;
    private int fuseTime = 30;
    private int lastFuseTime;
    private int currentFuseTime;
    private UUID summoner;

    private AnimationFactory factory = new AnimationFactory(this);

    public OnionEntity(EntityType<OnionEntity> entityType, World world) {
        this(entityType, world, null);
    }

    public OnionEntity(EntityType<OnionEntity> entityType, World world, @Nullable UUID summoner) {
        super(entityType, world);
        if (summoner != null) {
            this.summoner = summoner;
            this.targetSelector.add(1, new ChaseAllButSummonerGoal(this, LivingEntity.class, this.summoner, true));
        }
    }

    // region animation & sound registering
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving()) {

            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.onion.walk", true));
            return PlayState.CONTINUE;
        }

        if (this.dataTracker.get(IGNITED)) {

            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.onion.explode", false));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.onion.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<OnionEntity> controller = new AnimationController<OnionEntity>(this, "controller",
                0, this::predicate);

        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {

        return this.factory;
    }
    // endregion

    @Override
    public EntityData initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
        entityData = super.initialize(serverWorldAccess, difficulty, spawnReason, entityData, entityTag);
        this.initEquipment(difficulty);

        return entityData;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, ConfigInit.CONFIG.onionEntityMaxHealth)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, ConfigInit.CONFIG.onionEntityMovSpeed)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, ConfigInit.CONFIG.onionEntityFollowRange);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new OnionIgniteGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        //this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
    }

    //region getter & setter
    public UUID getSummoner() {
        return this.summoner;
    }

    public int getFuseSpeed() {
        return this.dataTracker.get(FUSE_SPEED);
    }

    public void setFuseSpeed(int fuseSpeed) {
        this.dataTracker.set(FUSE_SPEED, fuseSpeed);
    }
    //endregion

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FUSE_SPEED, -1);
        this.dataTracker.startTracking(IGNITED, false);
    }


    //region ignite & fuse
    public boolean isIgnited() {
        return this.dataTracker.get(IGNITED);
    }

    public void ignite() {
        this.dataTracker.set(IGNITED, true);
    }
    //endregion

    //region NBT
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putShort("Fuse", (short)this.fuseTime);
        nbt.putByte("ExplosionRadius", (byte)this.explosionRadius);
        nbt.putBoolean("ignited", this.isIgnited());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Fuse", 99)) {
            this.fuseTime = nbt.getShort("Fuse");
        }
        if (nbt.contains("ExplosionRadius", 99)) {
            this.explosionRadius = nbt.getByte("ExplosionRadius");
        }
        if (nbt.getBoolean("ignited")) {
            this.ignite();
        }
    }
    //endregion

    @Override
    public void tick() {
        if (world.isClient()) return;

        if (this.isAlive()) {
            int i;
            this.lastFuseTime = this.currentFuseTime;
            if (this.isIgnited()) {
                this.setFuseSpeed(1);
            }
            if ((i = this.getFuseSpeed()) > 0 && this.currentFuseTime == 0) {
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                this.dataTracker.set(IGNITED, true);
                this.emitGameEvent(GameEvent.PRIME_FUSE);
            }
            this.currentFuseTime += i;
            if (this.currentFuseTime < 0) {
                this.currentFuseTime = 0;
            }
            if (this.currentFuseTime >= this.fuseTime) {
                this.currentFuseTime = this.fuseTime;

                List<PlayerEntity> nearbyPlayers = PlayerStream.around(world, this.getBlockPos(), explosionRadius).toList();    //FIXME: grab all livingentities instead of players

                this.explode();
                for (PlayerEntity player : nearbyPlayers) {
                    if (!player.isAlive()) {
                        ExperienceOrbEntity.spawn((ServerWorld)this.world, player.getPos(), 75);
                    }
                }
            }
        }
        super.tick();
    }

    //region explosion & effect
    private void explode() {
        if (!this.world.isClient()) {

            Explosion.DestructionType destructionType;

            if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ||
                    !ConfigInit.CONFIG.onionEntityEnvironmentalDamage) {
                destructionType = Explosion.DestructionType.NONE;
            }
            else {
                destructionType = Explosion.DestructionType.BREAK;
            }

            this.dead = true;
            this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius, destructionType);
            this.discard();
            this.spawnEffectsCloud();
        }
    }

    private void spawnEffectsCloud() {
        StatusEffectInstance statusEffect = new StatusEffectInstance(StatusEffects.POISON, 80, 1);

        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.world, this.getX(), this.getY(), this.getZ());
        areaEffectCloudEntity.setRadius(effectRadius);
        areaEffectCloudEntity.setRadiusOnUse(-0.5f);
        areaEffectCloudEntity.setWaitTime(10);
        areaEffectCloudEntity.setDuration(areaEffectCloudEntity.getDuration() / 2);
        areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());

        areaEffectCloudEntity.addEffect(new StatusEffectInstance(statusEffect));

        this.world.spawnEntity(areaEffectCloudEntity);
    }
    //endregion

    //region spawn
    public float getSpawnTime(float timeDelta) {
        return MathHelper.lerp(timeDelta, this.lastFuseTime, this.currentFuseTime) / (float)(this.fuseTime - 2);
    }
    //endregion

    //region sound
    @Override
    protected SoundEvent getAmbientSound() {

        int chance = world.random.nextInt(0,2);

        if (chance < 1) { return NeMuelchSounds.ENTITY_ONION_SQUEEL_ONE; }
        if (chance < 2) { return NeMuelchSounds.ENTITY_ONION_SQUEEL_TWO; }
        else return NeMuelchSounds.ENTITY_ONION_SQUEEL_THREE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return NeMuelchSounds.ENTITY_ONION_SQUEEL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeMuelchSounds.ENTITY_ONION_SQUEEL_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(NeMuelchSounds.ENTITY_ONION_FLAP, 0.15f, 1.0f);
    }
    //endregion


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.summoner != null) nbt.putUuid("Summoner", this.summoner);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Summoner")) this.summoner = nbt.getUuid("Summoner");
    }
}