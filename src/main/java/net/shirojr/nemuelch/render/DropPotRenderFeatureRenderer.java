package net.shirojr.nemuelch.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.client.DropPotEntityModel;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;

public class DropPotRenderFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    private final DropPotEntityModel<DropPotEntity> model;

    public DropPotRenderFeatureRenderer(LivingEntityRenderer<T, M> context, EntityModelLoader loader) {
        super(context);
        this.model = new DropPotEntityModel<>(loader.getModelPart(NeMuelchClient.DROP_POT_LAYER));
    }

    @Override
    protected Identifier getTexture(T entity) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/drop_pot.png");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle,
                       float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.isFallFlying()) return;
        boolean shouldRender = entity.getMainHandStack().getItem() instanceof DropPotBlockItem ||
                entity.getOffHandStack().getItem() instanceof DropPotBlockItem;
        if (!shouldRender) {
            for (ItemStack armorStack : entity.getArmorItems()) {
                if (armorStack.getItem() instanceof DropPotBlockItem) {
                    shouldRender = true;
                    break;
                }
            }
        }
        if (!shouldRender) {
            if (entity instanceof PlayerEntity player) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    if (player.getInventory().getStack(i).getItem() instanceof DropPotBlockItem) {
                        shouldRender = true;
                        break;
                    }
                }
            }
        }
        if (!shouldRender) return;

        matrices.push();
        float scale = 0.5f;
        matrices.scale(scale, scale, scale);
        if (entity.isFallFlying()) {
            float pitch = entity.getPitch(tickDelta);
            // float yaw = entity.getYaw(tickDelta);

            matrices.translate(0, 1.2, -0.5);
            matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(pitch + 90));
            // matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
        }
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(getTexture(entity)));
        this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
        matrices.pop();
    }
}
