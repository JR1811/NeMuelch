package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.entity.custom.DiceEntity;
import net.shirojr.nemuelch.entity.misc.DiceRollPlan;
import org.joml.Quaternionf;

public class DiceEntityRenderer extends EntityRenderer<DiceEntity> {
    private static final float MODEL_SIZE = 8f / 16f;

    public DiceEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(DiceEntity entity) {
        return null;
    }

    @Override
    public void render(DiceEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        float rollTicksWithDelta = entity.getRollTicks() < 0 ? -1f : entity.getRollTicks() + tickDelta;

        matrices.push();
        Quaternionf rotation;
        float size = entity.getWidth() / MODEL_SIZE;
        float height = 0f;
        if (rollTicksWithDelta < 0) {
            rotation = new Quaternionf(entity.getToRot());
        } else {
            DiceRollPlan rollPlan = entity.getRollPlan();
            height = rollPlan.heightAt(rollTicksWithDelta);
            rotation = rollPlan.rotationAt(rollTicksWithDelta, entity.getFromRot(), entity.getToRot(), new Quaternionf());
        }
        matrices.translate(0, entity.getHeight() / 2f + height, 0);
        matrices.multiply(rotation);

        matrices.scale(size, size, size);
        matrices.translate(0f, 0.25f, 0f);
        MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getItemStack(), ModelTransformationMode.NONE,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());

        matrices.pop();
    }
}
