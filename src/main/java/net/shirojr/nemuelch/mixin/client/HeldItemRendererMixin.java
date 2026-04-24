package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.shirojr.nemuelch.item.custom.supportItem.SmokingPipeItem;
import net.shirojr.nemuelch.item.util.FirstPersonInvisible;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void renderStaticFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                             float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                                             VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SmokingPipeItem smokingPipeItem) || !smokingPipeItem.isInUse(stack)) return;
        matrices.push();
        //TODO: add translation for each item separately
        matrices.translate(0, 0, -0.5);
        itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers,
                player.getWorld(), player.getId());
        matrices.pop();
    }

    @WrapOperation(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void preventDefaultFirstPersonRendering(HeldItemRenderer instance, LivingEntity entity, ItemStack stack,
                                                    ModelTransformationMode renderMode, boolean leftHanded,
                                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                                    int light, Operation<Void> original) {
        if (FirstPersonInvisible.isInvisible(stack)) {
            if (renderMode.isFirstPerson()) {
                return;
            }
        }
        original.call(instance, entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
    }
}
