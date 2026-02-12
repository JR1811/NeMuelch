package net.shirojr.nemuelch.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.shirojr.nemuelch.init.NeMuelchItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MiasmaParticle extends Particle {
    private final ItemStack renderedStack;
    private final float initialScale;
    private final float pitchOffset, yawOffset, rollOffset;

    private float scale;

    public MiasmaParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                          float initialScale, int maxAge) {
        this(world, x, y, z, velocityX, velocityY, velocityZ, initialScale, maxAge,
                List.of(NeMuelchItems.MIASMA_MEDIUM.getDefaultStack(), NeMuelchItems.MIASMA_BIG.getDefaultStack()));
    }

    public MiasmaParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ,
                          float initialScale, int maxAge, List<ItemStack> miasmaStacks) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.initialScale = initialScale;
        this.scale = 0;
        this.maxAge = maxAge;
        Random random = this.world.random;
        this.renderedStack = miasmaStacks.get(random.nextInt(miasmaStacks.size()));

        float maxTilt = 20;

        this.pitchOffset = (random.nextFloat() - 0.5f) * 2 * maxTilt;
        this.yawOffset = (random.nextFloat() - 0.5f) * 2 * maxTilt;
        this.rollOffset = (random.nextFloat() - 0.5f) * 2 * maxTilt;
    }

    public ItemStack getRenderedStack() {
        return renderedStack;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.velocityX = this.velocityY = this.velocityZ = 0;
            this.move(this.velocityX, this.velocityY, this.velocityZ);
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        this.scale = getInterpolatedSize(tickDelta);

        if (this.scale <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        MatrixStack matrices = getMatrixStack(camera, tickDelta, this.scale);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.pitchOffset));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.yawOffset));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rollOffset));

        VertexConsumerProvider.Immediate entityVertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        MinecraftClient.getInstance().getItemRenderer().renderItem(
                this.getRenderedStack(),
                ModelTransformationMode.GROUND,
                15728880,
                OverlayTexture.DEFAULT_UV,
                matrices,
                entityVertexConsumers,
                world,
                0
        );
        entityVertexConsumers.draw();
    }

    private float getInterpolatedSize(float tickDelta) {
        int climaxAge = 5;
        float progress;
        float interpolated;

        float smoothAge = age + tickDelta;

        if (age > climaxAge) {
            progress = MathHelper.clamp((smoothAge + (float) maxAge / climaxAge) / maxAge, 0f, 1f);
            interpolated = MathHelper.lerp(progress * progress, this.initialScale, 0);
        } else {
            progress = MathHelper.clamp(smoothAge / climaxAge, 0f, 1f);
            interpolated = MathHelper.lerp(progress * progress, 0, this.initialScale);
        }
        return interpolated;
    }

    private MatrixStack getMatrixStack(Camera camera, float tickDelta, float scale) {
        MatrixStack matrices = new MatrixStack();

        Vec3d cameraPos = camera.getPos();
        float renderX = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.x);
        float renderY = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.y);
        float renderZ = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.z);

        matrices.translate(renderX, renderY, renderZ);

        matrices.scale(scale, scale, scale);
        return matrices;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {

        @Override
        public @Nullable Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z,
                                                 double velocityX, double velocityY, double velocityZ) {
            return new MiasmaParticle(world, x, y, z, velocityX, velocityY, velocityZ, 0.75f, 100);
        }
    }
}
