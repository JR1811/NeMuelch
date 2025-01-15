package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;

public class PotLauncherEntityRenderer extends EntityRenderer<PotLauncherEntity> {
    private final PotLauncherEntityModel<PotLauncherEntity> baseModel;

    public PotLauncherEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.baseModel = new PotLauncherEntityModel<>(ctx.getPart(NeMuelchEntityModelLayers.POT_LAUNCHER));
    }

    @Override
    public Identifier getTexture(PotLauncherEntity entity) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/pot_launcher_entity.png");
    }

    @Override
    public boolean shouldRender(PotLauncherEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(PotLauncherEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (this.dispatcher.shouldRenderHitboxes()) {
            renderInteractionBoxes(entity, matrices, vertexConsumers);
        }

        float baseScale = 1.6f;
        float potScale = 3.0f;

        matrices.push();
        matrices.translate(0, 1.25, 0); // tub height 0.35?
        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(entity.getAngles().getYaw()));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getAngles().getPitch()));
        matrices.scale(potScale, potScale, potScale);
        //TODO: render based on angles
        MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getPotSlot(), ModelTransformation.Mode.GROUND,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.hashCode());
        matrices.pop();

        matrices.push();
        matrices.translate(0.0, PotLauncherEntity.HEIGHT, 0.0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
        matrices.scale(baseScale, baseScale, baseScale);

        this.baseModel.setAngles(entity, tickDelta);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.baseModel.getLayer(getTexture(entity)));
        this.baseModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void renderInteractionBoxes(PotLauncherEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        entity.getInteractionBoxes().forEach((interactionHitBox, box) -> {
            Vec3f color = interactionHitBox.getDebugColor();
            WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.LINES), box,
                    color.getX(), color.getY(), color.getZ(), 1f);
        });
    }
}
