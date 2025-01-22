package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;

public class DropPotEntityRenderer extends EntityRenderer<DropPotEntity> {
    private final DropPotEntityModel<DropPotEntity> model;

    public DropPotEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new DropPotEntityModel<>(ctx.getPart(NeMuelchEntityModelLayers.DROP_POT));
    }

    @Override
    public Identifier getTexture(DropPotEntity entity) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/drop_pot.png");
    }

    @Override
    public boolean shouldRender(DropPotEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(DropPotEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        Vec3d motion = entity.getVelocity();
        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(getYaw(motion) - 90));
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getPitch(motion) - 90));

        float turbulenceSin = (float) Math.sin(entity.age + tickDelta);
        float turbulenceCos = (float) Math.cos(entity.age + tickDelta);
        float turbulenceIntensity = (float) (8.0 * entity.getVelocity().length());
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(turbulenceSin * turbulenceIntensity));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(turbulenceCos * turbulenceIntensity));

        matrices.translate(0.0, 1.0F, 0.0);

        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
        matrices.scale(0.7f, 0.7f, 0.7f);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    protected boolean hasLabel(DropPotEntity entity) {
        return false;
    }

    private static float getYaw(Vec3d motion) {
        return (float)(Math.atan2(motion.getZ(), motion.getX()) * (180.0 / Math.PI)) - 90.0F;
    }

    private static float getPitch(Vec3d motion) {
        return (float)(-Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)) * (180.0 / Math.PI));
    }

    @SuppressWarnings("unused")
    private static float getRoll(Vec3d motion) {
        if (motion.lengthSquared() == 0) return 0.0f;
        Vec3d normalizedMotion = motion.normalize();
        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d sideways = up.crossProduct(normalizedMotion);
        return (float) (Math.atan2(sideways.y, Math.sqrt(sideways.x * sideways.x + sideways.z * sideways.z)) * (180.0 / Math.PI));
    }
}
