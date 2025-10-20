package net.shirojr.nemuelch.event.custom;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaders;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.render.DropPotRenderFeatureRenderer;
import net.shirojr.nemuelch.render.TalismanChargeRenderer;

public class RenderEvents {
    public static void register() {
        ArmorRenderer.register(PortableBarrelRenderer::new, NeMuelchItems.PORTABLE_BARREL);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(RenderEvents::entityFeatureRendering);
        // WorldRenderEvents.LAST.register(RenderEvents::renderShadersWithoutGui);
        ShaderEffectRenderCallback.EVENT.register(RenderEvents::renderShaders);
        HudRenderCallback.EVENT.register(RenderEvents::renderLifeOnGui);
        WorldRenderEvents.AFTER_ENTITIES.register(TalismanChargeRenderer.getInstance());
    }

    private static void renderLifeOnGui(DrawContext context, float delta) {
        if (!NeMuelchConfigInit.CONFIG.guiBehaviour.enabledHpAmountTextRendering()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientPlayerEntity player = client.player;
        if (player == null || player.getWorld() == null || player.isCreative() || player.isSpectator()) return;
        if (player.getHealth() > NeMuelchConfigInit.CONFIG.guiBehaviour.getMinHpTextRenderingAmount()) return;
        String health = player.getHealth() + " / " + player.getMaxHealth() + " HP";
        int x = client.getWindow().getScaledWidth() / 2 + NeMuelchConfigInit.CONFIG.guiBehaviour.getHpTextRenderingPosX();
        int y = client.getWindow().getScaledHeight() - 10 - NeMuelchConfigInit.CONFIG.guiBehaviour.getHpTextRenderingPosY();
        context.drawText(client.textRenderer, health, x, y, 14737632, true);
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
