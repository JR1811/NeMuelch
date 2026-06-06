package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.camera.Displacement;
import net.shirojr.nemuelch.camera.DisplacementSequence;
import net.shirojr.nemuelch.client.NeMuelchCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements AutoCloseable {

    /**
     * @implNote Movement is handled in the {@link CameraMixin} class
     */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"))
    private void tiltForCameraShake(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        DisplacementSequence activeSequence = NeMuelchCache.CAMERA_SHAKE_HANDLER.getActiveDisplacementSequence();
        if (activeSequence == null) return;
        Displacement displacement = activeSequence.getInterpolatedDisplacement(tickDelta);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(displacement.getRoll()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(displacement.getYaw()));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(displacement.getPitch()));
    }
}
