package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;

public class DropPotEntityRenderer extends EntityRenderer<DropPotEntity> {
    private final DropPotEntityModel<DropPotEntity> model;

    public DropPotEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new DropPotEntityModel<>(ctx.getPart(NeMuelchClient.DROP_POT_LAYER));
    }

    @Override
    public Identifier getTexture(DropPotEntity entity) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/drop_pot.png");
    }

    @Override
    public void render(DropPotEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.translate(0.0, 0.15F, 0.0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));

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
