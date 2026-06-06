package net.shirojr.nemuelch.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.particle.data.SwipeParticleEffect;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SwipeParticle extends SpriteBillboardParticle {
    private final SpriteProvider sprites;
    public float yaw;
    public float pitch;

    public SwipeParticle(ClientWorld level, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                         SwipeParticleEffect effect, SpriteProvider sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.yaw = effect.yaw();
        this.pitch = effect.pitch();
        this.maxAge = effect.maxAge();
        this.scale = effect.scale();
        this.setColor(effect.color(), false);
        this.setAlpha(0f);
        this.sprites = sprites;
        this.setSpriteForAge(sprites);
        this.velocityX = 0;
        this.velocityY = effect.direction() == SwipeParticleEffect.Direction.UP ? 0.025 : -0.025;
        this.velocityZ = 0;
    }

    @Override
    public void tick() {
        float peakProgress = 0.25f;
        float progress = MathHelper.clamp(((float) this.age + 1) / this.maxAge, 0, 1);
        float alpha;
        if (progress < peakProgress) alpha = progress / peakProgress;
        else alpha = 1 - ((progress - peakProgress) / (1 - peakProgress));

        this.setAlpha(alpha);

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        if (this.age++ >= this.maxAge) {
            this.markDead();
        }
        this.setSpriteForAge(this.sprites);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d camPos = camera.getPos();
        float interpolatedX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.getX());
        float interpolatedY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.getY());
        float interpolatedZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.getZ());

        Quaternionf rotations;
        float yaw = camera.getYaw();
        float interpolatedAngle = MathHelper.lerp(tickDelta, this.prevAngle, this.angle);
        rotations = new Quaternionf().rotateY((float) Math.toRadians(-yaw));
        if (interpolatedAngle != 0.0F) {
            rotations.rotateZ(interpolatedAngle);
        }

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float size = this.getSize(tickDelta);

        for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
            Vector3f cornerPos = corners[cornerIndex];
            cornerPos.rotate(rotations);
            cornerPos.mul(size);
            cornerPos.add(interpolatedX, interpolatedY, interpolatedZ);
        }

        float minU = this.getMinU();
        float maxU = this.getMaxU();
        float minV = this.getMinV();
        float maxV = this.getMaxV();
        int brightness = this.getBrightness(tickDelta);
        vertexConsumer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).texture(maxU, maxV).color(this.red, this.green, this.blue, this.alpha).light(brightness).next();
        vertexConsumer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).texture(maxU, minV).color(this.red, this.green, this.blue, this.alpha).light(brightness).next();
        vertexConsumer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).texture(minU, minV).color(this.red, this.green, this.blue, this.alpha).light(brightness).next();
        vertexConsumer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).texture(minU, maxV).color(this.red, this.green, this.blue, this.alpha).light(brightness).next();
    }

    @Override
    protected int getBrightness(float tint) {
        return LightmapTextureManager.MAX_LIGHT_COORDINATE;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @SuppressWarnings("SameParameterValue")
    private void setColor(int color, boolean includeAlpha) {
        this.setColor(
                ColorHelper.Argb.getRed(color) / 255f,
                ColorHelper.Argb.getGreen(color) / 255f,
                ColorHelper.Argb.getBlue(color) / 255f
        );
        if (includeAlpha) {
            this.setAlpha(ColorHelper.Argb.getAlpha(color) / 255f);
        }
    }

    public record Factory(SpriteProvider sprites) implements ParticleFactory<SwipeParticleEffect> {
        @Override
        public Particle createParticle(SwipeParticleEffect parameters, ClientWorld world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new SwipeParticle(world, x, y, z, velocityX, velocityY, velocityZ, parameters, this.sprites);
        }
    }
}
