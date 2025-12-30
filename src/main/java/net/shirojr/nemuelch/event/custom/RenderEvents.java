package net.shirojr.nemuelch.event.custom;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.render.DropPotRenderFeatureRenderer;
import net.shirojr.nemuelch.render.TalismanChargeRenderer;
import net.shirojr.nemuelch.util.helper.PullUpFeatureHelper;

public class RenderEvents {
    private static final Identifier ICONS_TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/icons.png");

    public static void register() {
        ArmorRenderer.register(PortableBarrelRenderer::new, NeMuelchItems.PORTABLE_BARREL);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(RenderEvents::entityFeatureRendering);
        ShaderEffectRenderCallback.EVENT.register(RenderEvents::renderShaders);
        HudRenderCallback.EVENT.register(RenderEvents::renderLifeOnGui);
        HudRenderCallback.EVENT.register(RenderEvents::renderPullUpIcon);
        WorldRenderEvents.AFTER_ENTITIES.register(TalismanChargeRenderer.getInstance());
    }

    private static void renderPullUpIcon(DrawContext context, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        if (!PullUpFeatureHelper.canPullUp(client.player, client.targetedEntity)) return;

        int width = 6;
        int height = 7;
        int spread = 7;
        int centerX = context.getScaledWindowWidth() / 2 - (width / 2) - 1;
        int centerY = context.getScaledWindowHeight() / 2 - (height / 2);

        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO
        );
        context.drawTexture(ICONS_TEXTURE, centerX + spread, centerY - spread, 16, 0, width, height);
        RenderSystem.defaultBlendFunc();
    }

    private static void renderLifeOnGui(DrawContext context, float delta) {
        if (!NeMuelchConfigInit.CONFIG.guiBehaviour.enabledHpAmountTextRendering()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientPlayerEntity player = client.player;
        if (player == null || player.getWorld() == null || player.isCreative() || player.isSpectator()) return;
        int minHpForDisplay = NeMuelchConfigInit.CONFIG.guiBehaviour.getMinHpTextRenderingAmount();
        int maxHpForDisplay = NeMuelchConfigInit.CONFIG.guiBehaviour.getMaxHpTextRenderingAmount();
        if (player.getHealth() < minHpForDisplay || player.getHealth() > maxHpForDisplay) return;
        String health = player.getHealth() + " / " + player.getMaxHealth() + " HP";
        int x = client.getWindow().getScaledWidth() / 2 + NeMuelchConfigInit.CONFIG.guiBehaviour.getHpTextRenderingPosX();
        int y = client.getWindow().getScaledHeight() - 10 - NeMuelchConfigInit.CONFIG.guiBehaviour.getHpTextRenderingPosY();
        context.drawText(client.textRenderer, health, x, y, 14737632, true);
    }

    private static void renderShaders(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NeMuelch.isSatinModLoaded()) return;
        if (client.player == null) return;
        NeMuelchShaderManager.FADE.updateStates(tickDelta);
        NeMuelchShaderManager.FADE.render();
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
