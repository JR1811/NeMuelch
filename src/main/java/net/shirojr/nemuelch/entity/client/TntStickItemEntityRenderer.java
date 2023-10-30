package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;
import net.shirojr.nemuelch.item.NeMuelchItems;

public class TntStickItemEntityRenderer extends EntityRenderer<TntStickItemEntity> {
    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/item/tnt_stick.png");
    private final ItemRenderer itemRenderer;

    public TntStickItemEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public Identifier getTexture(TntStickItemEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(TntStickItemEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(TntStickItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        float scale = 0.8f;
        if (entity.age < 2 && this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 12.25) return;

        matrices.push();
        matrices.scale(scale, scale, scale);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));

        this.itemRenderer.renderItem(NeMuelchItems.TNT_STICK.getDefaultStack(),
                ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers,
                entity.getId());

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
