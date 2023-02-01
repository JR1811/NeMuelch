package net.shirojr.nemuelch.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckObjectsFeatureRenderer.class)
public abstract class StuckObjectsFeatureRendererMixin<T extends LivingEntity, M extends PlayerEntityModel<T>>
        extends FeatureRenderer<T, M> {

    @Shadow protected abstract int getObjectCount(T var1);

    public StuckObjectsFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity,
                        float f, float g, float h, float j, float k, float l, CallbackInfo info) {

        if (livingEntity instanceof PlayerEntity player && ConfigInit.CONFIG.startRenderingArrowsFunctionality) {
            if (this.getObjectCount(livingEntity) <= 0) return;

            if (player.getHealth() >= ConfigInit.CONFIG.startRenderingArrowsAtHealth) {
                info.cancel();
            }
        }
    }
}
