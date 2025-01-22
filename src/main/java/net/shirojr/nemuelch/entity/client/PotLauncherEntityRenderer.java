package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
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
            this.renderInteractionBoxes(entity, matrices, vertexConsumers);
        }
        this.renderDropPotItem(entity, matrices, vertexConsumers, light, tickDelta);
        this.renderLeash(entity, tickDelta, matrices, vertexConsumers);
        this.renderLauncher(entity, matrices, vertexConsumers, light, tickDelta);

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }


    private void renderInteractionBoxes(PotLauncherEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        entity.getInteractionBoxes().forEach((interactionHitBox, box) -> {
            Vec3f color = interactionHitBox.getDebugColor();
            WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.LINES), box,
                    color.getX(), color.getY(), color.getZ(), 1f);
        });
    }

    private void renderDropPotItem(PotLauncherEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        float potScale = 3.0f;
        float pivotY = 0.9f, pivotZ = 0.5f;

        matrices.push();

        matrices.translate(0, 1.25 + pivotY, 0);
        matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(entity.getAngles().getYaw()));
        matrices.translate(0, 0, pivotZ);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getAngles().getPitch()));
        matrices.translate(0, 0 - pivotY, -pivotZ - 1.3);
        float interpolatedTicks = entity.isActivated() ? entity.getActivationTicks() + tickDelta : 0;
        float normalizedPosition = interpolatedTicks / PotLauncherEntity.ACTIVATION_DURATION;
        matrices.translate(0, 0, MathHelper.lerp(normalizedPosition * normalizedPosition * normalizedPosition, 0.0f, 1.5f));

        matrices.scale(potScale, potScale, potScale);

        MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getPotSlot(), ModelTransformation.Mode.GROUND,
                light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.hashCode());

        matrices.pop();
    }

    private void renderLeash(PotLauncherEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider) {
        Entity leashHolderEntity = entity.getCachedLeashHolder();
        if (leashHolderEntity == null) return;

        matrices.push();
        Vec3d holderPos = leashHolderEntity.getLeashPos(tickDelta);

        double yawInRad = Math.toRadians(entity.getAngles().getYaw()) + (Math.PI / 2);
        Vec3d leashOffset = entity.getLeashOffset();
        double relativeXOffset = Math.cos(yawInRad) * leashOffset.z + Math.sin(yawInRad) * leashOffset.x;
        double relativeZOffset = Math.sin(yawInRad) * leashOffset.z - Math.cos(yawInRad) * leashOffset.x;
        Vec3d attachmentPos = new Vec3d(
                entity.getX() + relativeXOffset,
                entity.getY() + leashOffset.y,
                entity.getZ() + relativeZOffset
        );

        matrices.translate(relativeXOffset, attachmentPos.getY() - entity.getY(), relativeZOffset);


        Vec3d direction = holderPos.subtract(attachmentPos);
        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getLeash());
        Matrix4f transformationMatrices = matrices.peek().getPositionMatrix();

        int entityLight = this.getBlockLight(entity, entity.getBlockPos());
        int holderLight = this.dispatcher.getRenderer(leashHolderEntity).getBlockLight(leashHolderEntity, new BlockPos(leashHolderEntity.getCameraPosVec(tickDelta)));
        int entitySkyLight = entity.getWorld().getLightLevel(LightType.SKY, entity.getBlockPos());
        int holderSkyLight = entity.getWorld().getLightLevel(LightType.SKY, leashHolderEntity.getBlockPos());

        int segments = 24;
        float segmentThickness = 0.125f;
        float normalizedLength = (float) (MathHelper.fastInverseSqrt(direction.x * direction.x + direction.z * direction.z) * 0.025F / 2.0F);
        float offsetXThickness = (float) (normalizedLength * direction.z);
        float offsetZThickness = (float) (normalizedLength * direction.x);

        for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
            MobEntityRenderer.renderLeashPiece(vertexConsumer, transformationMatrices,
                    (float) direction.x, (float) direction.y, (float) direction.z,
                    entityLight, holderLight, entitySkyLight, holderSkyLight,
                    segmentThickness, segmentThickness, offsetXThickness, offsetZThickness,
                    segmentIndex, false);
        }
        for (int segmentIndex = segments; segmentIndex >= 0; segmentIndex--) {
            MobEntityRenderer.renderLeashPiece(vertexConsumer, transformationMatrices,
                    (float) direction.x, (float) direction.y, (float) direction.z,
                    entityLight, holderLight, entitySkyLight, holderSkyLight,
                    segmentThickness, segmentThickness, offsetXThickness, offsetZThickness,
                    segmentIndex, true);
        }
        matrices.pop();
    }

    private void renderLauncher(PotLauncherEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        float baseScale = 1.6f;

        matrices.push();
        matrices.translate(0.0, PotLauncherEntity.HEIGHT, 0.0);
        matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180));
        matrices.scale(baseScale, baseScale, baseScale);

        this.baseModel.setAngles(entity, tickDelta);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.baseModel.getLayer(getTexture(entity)));
        this.baseModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }
}
