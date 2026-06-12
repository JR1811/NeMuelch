package net.shirojr.nemuelch.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;

public class BlockFinderRenderer implements WorldRenderEvents.AfterTranslucent {
    public static final HashSet<BlockPos> DISPLAYED_POS = new HashSet<>();

    @Override
    public void afterTranslucent(WorldRenderContext context) {
        if (DISPLAYED_POS.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        float normalizedAlphaPulse = (MathHelper.sin((player.age + context.tickDelta()) * 0.1f) + 1f) * 0.5f;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        BufferBuilder bufferBuilder = new BufferBuilder(400 * DISPLAYED_POS.size());
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        MatrixStack matrixStack = context.matrixStack();

        matrixStack.push();
        Vec3d cam = context.camera().getPos();
        matrixStack.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : DISPLAYED_POS) {
            Box box = new Box(pos);
            WorldRenderer.renderFilledBox(matrixStack, bufferBuilder,
                    box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
                    0.2f, 0.6f, 0.3f, MathHelper.lerp(normalizedAlphaPulse, 0f, 0.6f));
        }
        matrixStack.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
