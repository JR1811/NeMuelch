package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.LiftPlatformEntity;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;

public class LiftPlatformRenderer extends EntityRenderer<LiftPlatformEntity> {
    private final LiftPlatformModel<LiftPlatformEntity> baseModel;

    public LiftPlatformRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.baseModel = new LiftPlatformModel<>(ctx.getPart(NeMuelchEntityModelLayers.LIFT_PLATFORM));
    }

    @Override
    public Identifier getTexture(LiftPlatformEntity entity) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/lift_platform.png");
    }

    @Override
    public boolean shouldRender(LiftPlatformEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(LiftPlatformEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((entity.age + tickDelta) % 360));
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.baseModel.getLayer(getTexture(entity)));
        this.baseModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
