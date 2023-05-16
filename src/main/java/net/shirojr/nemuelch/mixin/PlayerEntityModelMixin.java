package net.shirojr.nemuelch.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.rendering.feature.HideBodyPartFeatureRenderer;
import net.shirojr.nemuelch.util.BodyParts;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityModelMixin extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public PlayerEntityModelMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    public void render(AbstractClientPlayerEntity clientPlayer, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        NbtCompound nbt = ((IBodyPartSaver)clientPlayer).getPersistentData();
        setVisible(nbt);
    }

    protected void setVisible(NbtCompound nbt) {
        this.model.head.visible = true;
        this.model.hat.visible = true;
        this.model.body.visible = true;
        this.model.rightArm.visible = true;
        this.model.leftArm.visible = true;
        this.model.rightLeg.visible = true;
        this.model.leftLeg.visible = true;

        if (nbt.contains(BodyParts.HEAD.getBodyPartName())) {
            this.model.head.visible = false;
        }
        if (nbt.contains(BodyParts.HAT.getBodyPartName())) {
            this.model.hat.visible = false;
        }
        if (nbt.contains(BodyParts.BODY.getBodyPartName())) {
            this.model.body.visible = false;
        }
        if (nbt.contains(BodyParts.RIGHT_ARM.getBodyPartName())) {
            this.model.rightArm.visible = false;
        }
        if (nbt.contains(BodyParts.LEFT_ARM.getBodyPartName())) {
            this.model.leftArm.visible = false;
        }
        if (nbt.contains(BodyParts.RIGHT_LEG.getBodyPartName())) {
            this.model.rightLeg.visible = false;
        }
        if (nbt.contains(BodyParts.LEFT_LEG.getBodyPartName())) {
            this.model.leftLeg.visible = false;
        }
    }
}
