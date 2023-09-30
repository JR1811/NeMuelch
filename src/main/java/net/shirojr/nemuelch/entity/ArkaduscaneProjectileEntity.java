package net.shirojr.nemuelch.entity;

import net.dehydration.init.EffectInit;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.revive.ReviveMain;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.network.EntitySpawnPacket;

import java.util.List;

public class ArkaduscaneProjectileEntity extends ThrownEntity {

    public ArkaduscaneProjectileEntity(EntityType<? extends ArkaduscaneProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public ArkaduscaneProjectileEntity(LivingEntity owner, World world) {
        super(NeMuelchEntities.ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE, owner, world);
    }

    public ArkaduscaneProjectileEntity(World world, double x, double y, double z) {
        super(NeMuelchEntities.ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE, x, y, z, world);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return EntitySpawnPacket.create(this);
    }

    @Override
    protected void initDataTracker() { }

    @Override
    public void tick() {

        super.tick();

        Vec3d vec3d = this.getVelocity();

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.onCollision(hitResult);
        double x = this.getX() + vec3d.x;
        double y = this.getY() + vec3d.y;
        double z = this.getZ() + vec3d.z;

        this.updateRotation();

        float speedReduction = 0.75f;
        float bulletDrop = -0.005f;

        if (this.world.getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
            this.discard();
            return;
        }

        this.setVelocity(vec3d.multiply(speedReduction));

        // if projectile experiences gravity
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, bulletDrop, 0.0));
        }

        this.setPosition(x, y, z);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {

        super.onEntityHit(entityHitResult);

        Entity entity = entityHitResult.getEntity();

        if (world.isClient) { return; }

        ServerWorld serverWorld = (ServerWorld) this.world;

        if (entity instanceof LivingEntity target) {

            if (target instanceof HostileEntity) {

                target.damage(DamageSource.MAGIC, 7f);

                serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                        entity.getX(), entity.getY() + 1, entity.getZ(),
                        4, 1, 1, 1, 0.25);

                this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }

            if (target instanceof PlayerEntity) {

                target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 150, 1));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 150, 1));

                if (FabricLoader.getInstance().isModLoaded("revive")) {
                    if (target.isDead()) {
                        //player target gets revive option in the death screen (revive effect only works on dead entities)
                        target.addStatusEffect(new StatusEffectInstance(
                                ReviveMain.LIVELY_AFTERMATH_EFFECT, 600));
                        this.playSound(SoundEvents.ENTITY_BEE_STING, 1f, 1f);
                    }
                    else {
                        //target isn't dead so sound for not applying AFTERMATH_EFFECT (revive) is played
                        this.playSound(SoundEvents.ENTITY_AXOLOTL_DEATH, 1f, 1f);
                    }
                }
            }
        }

        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {

        super.onBlockHit(blockHitResult);

        if (world.isClient) { return; }

        int x = blockHitResult.getBlockPos().getX();
        int y = blockHitResult.getBlockPos().getY();
        int z = blockHitResult.getBlockPos().getZ();

        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.LAVA,
                x, y + 2, z, 10, 1, 1, 1, 2);

        if (FabricLoader.getInstance().isModLoaded("revive")) {
            List<Entity> targets = world.getOtherEntities(null, Box.of(blockHitResult.getPos(), 11, 6, 11));
            targets.forEach(entity -> {
                if (entity.isPlayer()) {
                    ((PlayerEntity) entity).addStatusEffect(new StatusEffectInstance(ReviveMain.AFTERMATH_EFFECT, 100));
                }
            });
        }

        this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1f, 1f);

        this.discard();

    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {

        super.onSpawnPacket(packet);

        double x = packet.getVelocityX();
        double y = packet.getVelocityY();
        double z = packet.getVelocityZ();

        for (int i = 0; i < 7; ++i) {

            double g = 0.4 + 0.1 * (double)i;
            this.world.addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), x * g, y, z * g);
        }

        this.setVelocity(x, y, z);
    }

}
