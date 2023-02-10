package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class IgnitedScorchingTntEntity extends TntEntity {

    private static final TrackedData<Integer> IGNITION =
            DataTracker.registerData(IgnitedScorchingTntEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_IGNITION = 80;

    public IgnitedScorchingTntEntity(World world, double x, double y, double z, @Nullable LivingEntity igniter) {
        super(world, x, y, z, igniter);
    }

    public void setIgnition(int ignition) { this.dataTracker.set(IGNITION, ignition); }

    public int getIgnition() {
        return this.dataTracker.get(IGNITION);
    }


    @Override
    public void tick() {
        if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0, -0.04, 0.0));
        }
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.98));
        if (this.onGround) {
            this.setVelocity(this.getVelocity().multiply(0.7, -0.5, 0.7));
        }
        int i = this.getIgnition() - 1;
        this.setIgnition(i);
        if (i <= 0) {
            this.discard();
            if (!this.world.isClient) {
                this.explode();
            }
        } else {
            this.updateWaterState();
            if (this.world.isClient) {
                this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        if (world.isClient) return;

        // TODO: launch custom projectiles
        this.world.createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, Explosion.DestructionType.BREAK);
    }
}
