package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.compat.cca.implementation.BoatDeepWaterComponent;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Debug(export = true)
@Mixin(BoatEntityRenderer.class)
public abstract class BoatEntityRendererMixin extends EntityRenderer<BoatEntity> {
    private BoatEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/vehicle/BoatEntity;interpolateBubbleWobble(F)F",
                    ordinal = 0
            )
    )
    private void renderDeepWaterEffect(BoatEntity boatEntity, float angle, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        BoatDeepWaterComponent component = BoatDeepWaterComponent.get(boatEntity);
        int deepWaterTicks = component.getDeepWaterTicks();
        if (deepWaterTicks <= 0) return;
        float progress = MathHelper.clamp((deepWaterTicks + tickDelta) / component.getMaxDeepWaterEnduranceTicks(), 0, 1);

        float tiltAngle = 50f * progress * progress;

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tiltAngle));

        float shakeIntensity = progress * 1.2f;
        float shakeSpeed = 8f + (progress * 12f);
        float rollShake = MathHelper.sin((deepWaterTicks + tickDelta) * shakeSpeed) * shakeIntensity;
        float yawShake = MathHelper.cos((deepWaterTicks + tickDelta) * shakeSpeed * 1.3f) * shakeIntensity;

        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollShake));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawShake * 0.5f));
    }
}
