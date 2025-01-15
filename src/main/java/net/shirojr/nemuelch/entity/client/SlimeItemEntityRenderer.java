package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.entity.custom.projectile.SlimeItemEntity;

public class SlimeItemEntityRenderer extends EntityRenderer<SlimeItemEntity> {
    private static final Identifier TEXTURE = new Identifier("textures/item/slime_ball.png");
    private final ItemRenderer itemRenderer;

    public SlimeItemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    protected int getBlockLight(SlimeItemEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public Identifier getTexture(SlimeItemEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(SlimeItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float scale = 0.8f;
        if (entity.age < 2 && this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25) {
            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));

        this.itemRenderer.renderItem(Items.SLIME_BALL.getDefaultStack(),
                ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers,
                entity.getId());

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
