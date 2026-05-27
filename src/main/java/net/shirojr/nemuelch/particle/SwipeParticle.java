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

    private static final Vector3f[] spriteCorners = new Vector3f[]{
            new Vector3f(-1.0f, -1.0f, 0.0f),
            new Vector3f(-1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, -1.0f, 0.0f),
    };

    public SwipeParticle(ClientWorld level, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                         SwipeParticleEffect effect, SpriteProvider sprites) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.yaw = effect.yaw();
        this.pitch = effect.pitch();
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.maxAge = 6;
        this.scale = 0.5f;
        this.setColor(effect.color(), false);
        this.setAlpha(0f);
        this.sprites = sprites;
        this.setSpriteForAge(sprites);
    }

    @Override
    public void tick() {
        float peakProgress = 0.25f;
        float progress = MathHelper.clamp(((float) this.age + 1) / this.maxAge, 0, 1);
        float alpha;
        if (progress < peakProgress) alpha = progress / peakProgress;
        else alpha = 1 - ((progress - peakProgress) / (1 - peakProgress));

        this.setAlpha(alpha);

        super.tick();
        this.setSpriteForAge(this.sprites);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        super.buildGeometry(vertexConsumer, camera, tickDelta);
        /*Quaternionf rotation = new Quaternionf();
        if (this.angle != 0) {
            rotation.rotateZ(MathHelper.lerp(tickDelta, this.prevAngle, this.angle));
        }
        rotation.rotateY((float) Math.toRadians(this.yaw));
        rotation.rotateZ((float) Math.toRadians(this.pitch));
        this.renderQuad(vertexConsumer, camera, rotation, tickDelta);*/
    }

    @Override
    protected int getBrightness(float tint) {
        return LightmapTextureManager.MAX_LIGHT_COORDINATE;
    }

    private void renderQuad(VertexConsumer vertexConsumer, Camera camera, Quaternionf rotation, float tickDelta) {
        Vec3d camPos = camera.getPos();
        float x = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.getX());
        float y = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.getY());
        float z = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.getZ());
        float size = this.getSize(tickDelta);
        int light = this.getBrightness(tickDelta);

        float[][] uvs = {
                {getMaxU(), getMaxV()},
                {getMaxU(), getMinV()},
                {getMinU(), getMinV()},
                {getMinU(), getMaxV()}
        };
        for (int cornerIndex = 0; cornerIndex < spriteCorners.length; cornerIndex++) {
            Vector3f corner = new Vector3f(spriteCorners[cornerIndex]).mul(size).rotate(rotation);
            vertexConsumer.vertex(x + corner.x, y + corner.y, z + corner.z)
                    .texture(uvs[cornerIndex][0], uvs[cornerIndex][1])
                    .color(this.red, this.green, this.blue, this.alpha).light(light).next();
        }
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
