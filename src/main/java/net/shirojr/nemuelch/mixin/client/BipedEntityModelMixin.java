package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.item.custom.supportItem.ClimbingPickItem;
import net.shirojr.nemuelch.render.EntityPosing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {
    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart rightLeg;

    @Shadow
    @Final
    public ModelPart leftLeg;

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void handleAdditionalRightArmPoses(T entity, CallbackInfo ci) {
        ItemStack stack = entity.getStackInHand(entity.getActiveHand());
        if (stack.getItem() instanceof ClimbingPickItem) {
            Vec3d hookPos = ClimbingPickItem.getHookPos(stack);
            if (hookPos != null) {
                EntityPosing.climb(rightArm, leftArm, head, true, hookPos, rightLeg, leftLeg);
                ci.cancel();
            }
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void handleAdditionalLeftArmPoses(T entity, CallbackInfo ci) {
        ItemStack stack = entity.getStackInHand(entity.getActiveHand());
        if (stack.getItem() instanceof ClimbingPickItem) {
            Vec3d hookPos = ClimbingPickItem.getHookPos(stack);
            if (hookPos != null) {
                EntityPosing.climb(rightArm, leftArm, head, false, hookPos, rightLeg, leftLeg);
                ci.cancel();
            }
        }
    }
}
