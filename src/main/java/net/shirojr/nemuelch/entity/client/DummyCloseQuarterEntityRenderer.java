package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;

public class DummyCloseQuarterEntityRenderer extends EntityRenderer<DummyCloseQuarterEntity> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/entity/dummy_cqc.png");

    private final DummyCloseQuarterEntityModel<DummyCloseQuarterEntity> model;

    public DummyCloseQuarterEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new DummyCloseQuarterEntityModel<>(ctx.getPart(NeMuelchEntityModelLayers.DUMMY_CQC));
    }

    @Override
    public Identifier getTexture(DummyCloseQuarterEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DummyCloseQuarterEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(getTexture(entity)));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        matrices.push();
        matrices.translate(0, 1.5, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        this.model.setAngles(entity, 0, 0, entity.age + tickDelta, 0, 0);
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrices.pop();
    }
}
