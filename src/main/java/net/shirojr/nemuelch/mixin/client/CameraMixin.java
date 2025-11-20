package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.shirojr.nemuelch.NeMuelchClient;
import net.shirojr.nemuelch.camera.CameraShakeHandler;
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

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    private float yaw;

    @Shadow
    private float pitch;

    @Inject(method = "update", at = @At("TAIL"))
    private void updateShakes(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!(focusedEntity instanceof ClientPlayerEntity player)) return;
        float tickDeltaAge = player.age + tickDelta;
        CameraShakeHandler cameraHandler = NeMuelchClient.CAMERA_SHAKE_HANDLER;
        cameraHandler.setTickDelta(tickDeltaAge);
        cameraHandler.update();
        if (cameraHandler.getFocusedEntity() == null || !cameraHandler.getFocusedEntity().equals(focusedEntity)) {
            cameraHandler.setFocusedEntity(focusedEntity);
        }

        setPos(getPos().add(cameraHandler.getDisplacement().getPosition()));
        setRotation(yaw + cameraHandler.getDisplacement().getYaw(), pitch + cameraHandler.getDisplacement().getPitch());
    }
}
