package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
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
        if (this.dispatcher.shouldRenderHitboxes()) {
            renderDebugStraps(entity, matrices, vertexConsumers);
        }
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.baseModel.getLayer(getTexture(entity)));
        this.baseModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void renderDebugStraps(LiftPlatformEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        Vec3d start = new Vec3d(0.39, 0.125, 0.67);
        Vec3d end = new Vec3d(0.462, 1.44, -0.67);
        renderDebugLine(matrices, vertexConsumers, start, end);
        renderDebugLine(matrices, vertexConsumers, start.multiply(-1, 1, 1), end.multiply(-1, 1, 1));
        renderDebugLine(matrices, vertexConsumers, start.multiply(1, 1, -1), end.multiply(1, 1, -1));
        renderDebugLine(matrices, vertexConsumers, start.multiply(-1, 1, -1), end.multiply(-1, 1, -1));

    }

    private static void renderDebugLine(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Vec3d start, Vec3d end) {
        /*Vec3d start = new Vec3d(0.39, 0.125, 0.67);
        Vec3d end = new Vec3d(0.462, 1.44, -0.67);*/

        matrices.push();
        matrices.translate(start.x, start.y, start.z);
        MatrixStack.Entry matrixEntry = matrices.peek();
        Matrix4f modelMatrix = matrixEntry.getPositionMatrix();
        Matrix3f normalMatrix = matrixEntry.getNormalMatrix();

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.LINES);
        buffer.vertex(modelMatrix, (float) start.x, (float) start.y, (float) start.z)
                .color(0, 255, 0, 255)
                .normal(normalMatrix, 0f, 1f, 0f)
                .next();
        buffer.vertex(modelMatrix, (float) end.x, (float) end.y, (float) end.z)
                .color(0, 255, 0, 255)
                .normal(normalMatrix, 0f, 1f, 0f)
                .next();
        matrices.pop();
    }
}
