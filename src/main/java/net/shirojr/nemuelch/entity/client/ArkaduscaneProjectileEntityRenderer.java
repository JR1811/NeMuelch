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
import net.shirojr.nemuelch.entity.custom.projectile.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.init.NeMuelchItems;

public class ArkaduscaneProjectileEntityRenderer extends EntityRenderer<ArkaduscaneProjectileEntity> {

    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/item/arkaduscane_projectile.png");
    private final ItemRenderer itemRenderer;


    public ArkaduscaneProjectileEntityRenderer(EntityRendererFactory.Context ctx) {

        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public int getBlockLight(ArkaduscaneProjectileEntity arkaduscaneProjectileEntity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(ArkaduscaneProjectileEntity arkaduscaneProjectileEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        if (arkaduscaneProjectileEntity.age < 2 && this.dispatcher.camera.getFocusedEntity()
                .squaredDistanceTo(arkaduscaneProjectileEntity) < 12.25) {
            super.render(arkaduscaneProjectileEntity, yaw, tickDelta, matrices, vertexConsumers, light);
            return;
        }

        matrices.push();
        float scale = 0.25f;
        matrices.scale(scale, scale, scale);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f));

        this.itemRenderer.renderItem(NeMuelchItems.ARKADUSCANE_ENTITY_PROJECTILE_ITEM.getDefaultStack(), ModelTransformation.Mode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, arkaduscaneProjectileEntity.getId());
        matrices.pop();
        super.render(arkaduscaneProjectileEntity, yaw, tickDelta, matrices, vertexConsumers, light);
    }


    @Override
    public Identifier getTexture(ArkaduscaneProjectileEntity entity) {
        return TEXTURE;
    }

}
