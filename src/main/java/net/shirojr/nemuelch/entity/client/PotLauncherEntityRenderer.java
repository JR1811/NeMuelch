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
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;

public class PotLauncherEntityRenderer extends EntityRenderer<PotLauncherEntity> {
    private final PotLauncherEntityModel<PotLauncherEntity> baseModel;
    // private final DropPotEntityModel<DropPotEntity> potModel;

    public PotLauncherEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.baseModel = new PotLauncherEntityModel<>(ctx.getPart(NeMuelchClient.POT_LAUNCHER_LAYER));
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
        float scale = 1.6f;

        matrices.push();
        matrices.translate(0.0, PotLauncherEntity.HEIGHT, 0.0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
        matrices.scale(scale, scale, scale);

        this.baseModel.setAngles(entity, 0.0F, 0.0F, 0.0F, entity.getAngles().getYaw(), entity.getAngles().getPitch());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.baseModel.getLayer(getTexture(entity)));
        this.baseModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();

        //TODO: render pot as projectile
        //TODO: render player as projectile
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}
