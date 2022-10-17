package net.shirojr.nemuelch.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.ai.custom.OnionIgniteGoal;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;


public class OnionEntity extends HostileEntity implements IAnimatable {

    private static final TrackedData<Integer> FUSE_SPEED = DataTracker.registerData(OnionEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IGNITED = DataTracker.registerData(OnionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    // TODO: Config implementation
    private int explosionRadius = 1;
    private float effectRadius = 10.0f;
    private int fuseTime = 40;
    private int lastFuseTime;
    private int currentFuseTime;
    private PlayerEntity summoner;


    private AnimationFactory factory = new AnimationFactory(this);

    public OnionEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        NeMuelch.LOGGER.info("executed ctor of entity");
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

        //controller.registerSoundListener(this::soundListener);

        animationData.addAnimationController(controller);
    }

    /*private <ENTITY extends IAnimatable> void soundListener(SoundKeyframeEvent<ENTITY> event) {

        if (event.sound.matches("walk")) {
            if (this.world.isClient) {
                this.getEntityWorld().playSound(this.getX(), this.getY(), this.getZ(),
                        NeMuelchSounds.ENTITY_ONION_FLAP, SoundCategory.HOSTILE, 0.25F, 1.0F, true);
            }
        }
    }*/

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
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 7.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 20.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new OnionIgniteGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this, new Class[0]));
        NeMuelch.LOGGER.info("initiating entity goals");
    }

    public boolean isSummoner(PlayerEntity player) {

        return getOnionSummoner() == player;    //TODO: might not be the right target?
    }

    //region getter & setter
    public PlayerEntity getOnionSummoner() {
        return this.summoner;
    }

    public void setOnionSummoner(PlayerEntity player) {
        this.summoner = player;
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
                this.explode();
            }
        }
        super.tick();
    }

    //region explosion & effect
    private void explode() {
        if (!this.world.isClient) {
            Explosion.DestructionType destructionType = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE;
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
}