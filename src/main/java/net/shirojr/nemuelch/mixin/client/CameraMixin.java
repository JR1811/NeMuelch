package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.camera.CameraShakeHandler;
import net.shirojr.nemuelch.camera.Displacement;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPos(Vec3d pos);

    @Shadow
    public abstract Vec3d getPos();

    @Shadow @Final private Vector3f diagonalPlane;

    @Shadow @Final private Vector3f verticalPlane;

    @Shadow @Final private Vector3f horizontalPlane;

    /**
     * @implNote Rotation is handled in the {@link GameRendererMixin} class
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void updateShakes(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        CameraShakeHandler cameraHandler = NeMuelchClient.CAMERA_SHAKE_HANDLER;

        if (cameraHandler.getFocusedEntity() == null || !cameraHandler.getFocusedEntity().equals(focusedEntity)) {
            cameraHandler.setFocusedEntity(focusedEntity);
        }
        if (!cameraHandler.isActive()) {
            return;
        }

        Displacement displacement = cameraHandler.getInterpolatedDisplacement(tickDelta);
        Vec3d localOffset = displacement.getPosition();
        Vec3d worldShake = new Vec3d(
                diagonalPlane.x * localOffset.x + verticalPlane.x * localOffset.y + horizontalPlane.x * localOffset.z,
                diagonalPlane.y * localOffset.x + verticalPlane.y * localOffset.y + horizontalPlane.y * localOffset.z,
                diagonalPlane.z * localOffset.x + verticalPlane.z * localOffset.y + horizontalPlane.z * localOffset.z
        );
        Vec3d newPos = getPos().add(worldShake);
        setPos(newPos);
    }
}
