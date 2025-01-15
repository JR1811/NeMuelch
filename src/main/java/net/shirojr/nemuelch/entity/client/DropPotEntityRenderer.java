package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
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

        float sin = (float) Math.sin(entity.age + tickDelta);
        float cos = (float) Math.cos(entity.age + tickDelta);
        float intensity = (float) (8.0 * entity.getVelocity().length());

        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(sin * intensity));
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(cos * intensity));

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
}
