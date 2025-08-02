package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.render.DropPotRenderFeatureRenderer;

public class RenderEvents {
    @SuppressWarnings("unchecked")
    public static void register() {
        ArmorRenderer.register(PortableBarrelRenderer::new, NeMuelchItems.PORTABLE_BARREL);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer.getModel() instanceof BipedEntityModel) {
                registrationHelper.register(new DropPotRenderFeatureRenderer<>((LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>>) entityRenderer, context.getModelLoader()));
            }
        });
    }
}
