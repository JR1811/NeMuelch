package net.shirojr.nemuelch.event.custom;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.client.AdvancedFogBlockEntityRenderer;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.camera.CameraUtil;
import net.shirojr.nemuelch.compat.cca.implementation.FleetingNotesComponent;
import net.shirojr.nemuelch.compat.cca.util.FleetingNoteData;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.entity.client.armor.PortableBarrelRenderer;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.render.BlockFinderRenderer;
import net.shirojr.nemuelch.render.DropPotRenderFeatureRenderer;
import net.shirojr.nemuelch.render.TalismanChargeRenderer;
import net.shirojr.nemuelch.util.helper.PullUpFeatureHelper;

public class RenderEvents {
    private static final Identifier ICONS_TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/icons.png");

    public static void register() {
        ArmorRenderer.register(PortableBarrelRenderer::new, NeMuelchItems.PORTABLE_BARREL);
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(RenderEvents::entityFeatureRendering);

        ShaderEffectRenderCallback.EVENT.register(RenderEvents::renderOverlayShaders);
        WorldRenderEvents.LAST.register(context -> renderWorldShaders(context.tickDelta()));

        HudRenderCallback.EVENT.register(RenderEvents::renderLifeOnGui);
        HudRenderCallback.EVENT.register(RenderEvents::renderPullUpIcon);
        HudRenderCallback.EVENT.register(RenderEvents::renderFleetingNotes);
        WorldRenderEvents.AFTER_ENTITIES.register(TalismanChargeRenderer.getInstance());
        WorldRenderEvents.BEFORE_ENTITIES.register(RenderEvents::renderAdvancedFogBlock);
        WorldRenderEvents.LAST.register(new BlockFinderRenderer());

        FluidRenderingEvents.initialize();
    }

    private static void renderAdvancedFogBlock(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;
        Vec3d camPos = context.camera().getPos();
        for (AdvancedFogBlockEntity blockEntity : ClientBlockEntityLoadingEvents.LOADED_ADVANCED_FOG_BLOCKS) {
            BlockPos pos = blockEntity.getPos();
            matrices.push();
            matrices.translate(
                    pos.getX() - camPos.x,
                    pos.getY() - camPos.y,
                    pos.getZ() - camPos.z
            );
            AdvancedFogBlockEntityRenderer.handleFaceRendering(matrices, consumers, blockEntity.getData());
            matrices.pop();
        }
    }

    private static void renderFleetingNotes(DrawContext drawContext, float tickDelta) {
        if (NeMuelchConfigInit.CONFIG.fleetingNotes.preventGeneralFleetingNotesRendering()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        if (client.currentScreen != null) return;
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if (world == null || player == null) return;
        FleetingNotesComponent component = FleetingNotesComponent.get(world);
        if (component.isEmpty()) return;
        Camera camera = client.gameRenderer.getCamera();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;

        Pair<Double, FleetingNoteData> closestEntry = null;
        for (var entry : component.getUnsyncedData()) {
            Vec3d notePos = entry.pos();
            FleetingNoteData data = entry.data();
            float maxDistance = data.getVisibleDistance();
            double sqDistance = camera.getPos().squaredDistanceTo(notePos);
            if (sqDistance > maxDistance * maxDistance) continue;
            if (!CameraUtil.isCrosshairOver(notePos, camera, data.getAngle())) continue;
            if (CameraUtil.hasObstruction(world, camera.getPos(), notePos, player)) continue;

            if (closestEntry == null || closestEntry.getLeft() > sqDistance) {
                closestEntry = new Pair<>(sqDistance, data);
            }
        }
        if (closestEntry != null) {
            drawContext.drawTooltip(client.textRenderer, closestEntry.getRight().getLines(), centerX + 25, centerY - 5);
        }
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

    private static void renderOverlayShaders(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NeMuelch.isSatinModLoaded()) return;
        if (client.player == null) return;
        NeMuelchShaderManager.FADE.getInstance().updateStates(tickDelta);
        NeMuelchShaderManager.FADE.getInstance().render();
    }

    private static void renderWorldShaders(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!NeMuelch.isSatinModLoaded()) return;
        if (client.player == null) return;
        NeMuelchShaderManager.CRIMSON_PHASE.getInstance().updateStates(tickDelta);
        NeMuelchShaderManager.CRIMSON_PHASE.getInstance().render();
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
