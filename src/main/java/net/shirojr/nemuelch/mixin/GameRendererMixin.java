package net.shirojr.nemuelch.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements SynchronousResourceReloader, AutoCloseable {
    @Shadow @Final private MinecraftClient client;
    @Unique private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/misc/slime_overlay.png");
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z",
            shift = At.Shift.AFTER))
    private void nemuelch$renderOverlays(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {

    }

    @Unique
    private void renderNausea(float distortionStrength) {
        int i = client.getWindow().getScaledWidth();
        int j = client.getWindow().getScaledHeight();
        double d = MathHelper.lerp(distortionStrength, 2.0, 1.0);
        float f = 0.2f * distortionStrength;
        float g = 0.4f * distortionStrength;
        float h = 0.2f * distortionStrength;
        double e = (double) i * d;
        double k = (double) j * d;
        double l = ((double) i - e) / 2.0;
        double m = ((double) j - k) / 2.0;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderColor(f, g, h, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(l, m + k, -90.0).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(l + e, m + k, -90.0).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(l + e, m, -90.0).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(l, m, -90.0).texture(0.0f, 0.0f).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
