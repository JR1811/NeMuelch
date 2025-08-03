package net.shirojr.nemuelch.event.custom;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaders;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.render.DropPotRenderFeatureRenderer;

public class RenderEvents {
    public static void register() {
        ArmorRenderer.register(PortableBarrelRenderer::new, NeMuelchItems.PORTABLE_BARREL);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(RenderEvents::entityFeatureRendering);
        // WorldRenderEvents.LAST.register(RenderEvents::renderShadersWithoutGui);
        ShaderEffectRenderCallback.EVENT.register(RenderEvents::renderShaders);
    }

    private static void renderShadersWithoutGui(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NeMuelch.isSatinPresent()) return;
        if (client.player == null) return;
        NeMuelchShaders.FADE.update(context.tickDelta());
        NeMuelchShaders.FADE.render();
    }

    private static void renderShaders(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NeMuelch.isSatinPresent()) return;
        if (client.player == null) return;
        NeMuelchShaders.FADE.update(tickDelta);
        NeMuelchShaders.FADE.render();
    }

    @SuppressWarnings("unchecked")
    private static void entityFeatureRendering(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> livingEntityRenderer,
                                               LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper,
                                               EntityRendererFactory.Context context) {
        if (livingEntityRenderer.getModel() instanceof BipedEntityModel) {
            registrationHelper.register(new DropPotRenderFeatureRenderer<>(
                    (LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>>) livingEntityRenderer,
                    context.getModelLoader()
            ));
        }
    }
}
