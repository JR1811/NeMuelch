package net.shirojr.nemuelch.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;

@SuppressWarnings("FieldCanBeLocal")
public class RottenMeatAirParticle extends SpriteBillboardParticle {
    public static final ParticleGroup PARTICLE_GROUP = new ParticleGroup(10000);
    public static final Vector3f[] COLORS = new Vector3f[]{
            new Vector3f(0.8F, 0.35F, 0.22F),
            new Vector3f(0.56F, 0.2F, 0.1F),
            new Vector3f(0.47F, 0.16F, 0.08F),
            new Vector3f(0.51F, 0.26F, 0.09F)
    };

    @SuppressWarnings("unused")
    private final SpriteProvider spriteProvider;
    protected int startScaleTick = -1;
    protected int startFadeTick = -1;

    public RottenMeatAirParticle(ClientWorld world, SpriteProvider spriteProvider, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSprite(this.spriteProvider);
        this.scale(0.7f);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (startFadeTick != -1) {
            float normalizedFadeProgress = (float) (this.age - startFadeTick) / (this.maxAge - startFadeTick);
            if (age >= startFadeTick) {
                this.alpha = 1.0f - normalizedFadeProgress;
            }
        }
        if (startScaleTick != -1) {
            float normalizedScaleProgress = (float) (this.age - startScaleTick) / (this.maxAge - startScaleTick);
            if (age >= startScaleTick) {
                this.scale(1.0f - normalizedScaleProgress * 0.5f);
            }
        }
        if (alpha == 0 || scale == 0) {
            this.markDead();
        }
        this.prevAngle = this.angle;
    }

    @Override
    public Optional<ParticleGroup> getGroup() {
        return Optional.of(PARTICLE_GROUP);
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            RottenMeatAirParticle particle = new RottenMeatAirParticle(world, this.spriteProvider, x, y, z, velocityX, velocityY, velocityZ);
            particle.maxAge = MathHelper.nextBetween(world.random, 200, 500);
            particle.startFadeTick = particle.maxAge - 100;
            particle.startScaleTick = particle.maxAge - 200;
            particle.gravityStrength = 0.05F;
            int colorIndex = world.getRandom().nextInt(COLORS.length - 1);
            Vector3f selectedColor = COLORS[colorIndex];
            particle.setColor(selectedColor.x, selectedColor.y, selectedColor.z);
            return particle;
        }
    }
}
