package net.shirojr.nemuelch.entity.custom.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.network.packet.EntitySpawnPacket;
import net.shirojr.nemuelch.sound.NeMuelchSounds;
import net.shirojr.nemuelch.util.helper.ExplosionHelper;

import java.util.List;

public class TntStickItemEntity extends ThrownItemEntity {
    private int tickCount = 60;
    private int bounces = -1;

    public TntStickItemEntity(World world, LivingEntity owner) {
        super(NeMuelchEntities.TNT_STICK_ITEM, owner, world);
    }

    public TntStickItemEntity(EntityType<TntStickItemEntity> slimeItemEntityEntityType, World world) {
        super(slimeItemEntityEntityType, world);
    }

    /**
     * Sets custom tick time of entity until it explodes. The default is <b><i>60 ticks</i></b>.
     */
    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }

    /**
     * Counts down bounces until it explodes
     *
     * @param bounces Set to <b><i>-1</i></b> to have no bounce counted explosion
     */
    public void setMaxBounces(int bounces) {
        this.bounces = bounces;
    }

    @Override
    protected Item getDefaultItem() {
        return NeMuelchItems.TNT_STICK;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(world instanceof ServerWorld serverWorld)) return;

        if (this.tickCount <= 0 || this.bounces == 0 || this.isOnFire()) {
            ExplosionHelper.explodeSpherical(serverWorld, this.getBlockPos(), 8);
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if (tickCount % 5 == 0) {
            Vec3d particleVelocity = this.getVelocity().multiply(0.3);
            serverWorld.spawnParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                    1, particleVelocity.x, particleVelocity.y, particleVelocity.z, 0);
        }

        this.tickCount--;
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        Vec3d incomingVector = this.getVelocity();
        Vec3d wallVector = Vec3d.of(blockHitResult.getSide().getVector());
        double bounceDampenerVertical = 0.4;
        double bounceDampenerHorizontal = 0.7;

        List<Direction> verticalSides = List.of(Direction.UP, Direction.DOWN);
        if (verticalSides.contains(blockHitResult.getSide())) {
            incomingVector = new Vec3d(incomingVector.x, (-incomingVector.y) * bounceDampenerVertical, incomingVector.z);
        } else {
            incomingVector = incomingVector.subtract(wallVector.multiply(incomingVector.dotProduct(wallVector) * 2));
            incomingVector = incomingVector.multiply(bounceDampenerHorizontal);
        }

        this.setVelocity(incomingVector);
        this.velocityModified = true;

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, blockHitResult.getBlockPos(),
                    NeMuelchSounds.TNT_STICK_DROP, SoundCategory.NEUTRAL, 0.7f, 1f);

            if (this.bounces > 0) this.bounces--;
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return EntitySpawnPacket.create(this);
    }
}
