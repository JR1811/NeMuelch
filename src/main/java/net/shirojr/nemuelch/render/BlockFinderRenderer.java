package net.shirojr.nemuelch.render;

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

public class BlockFinderRenderer implements WorldRenderEvents.Last {
    public static final HashSet<BlockPos> DISPLAYED_POS = new HashSet<>();

    @Override
    public void onLast(WorldRenderContext context) {
        if (DISPLAYED_POS.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        MatrixStack matrixStack = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        float normalizedAlphaPulse = (MathHelper.sin((player.age + context.tickDelta()) * 0.1f) + 1f) * 0.5f;

        RenderLayer NO_DEPTH = RenderLayer.of(
                "block_finder",
                VertexFormats.POSITION_COLOR,
                VertexFormat.DrawMode.TRIANGLE_STRIP,
                256,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.COLOR_PROGRAM)
                        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .target(RenderPhase.MAIN_TARGET)
                        .writeMaskState(RenderPhase.COLOR_MASK)
                        .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .build(false)
        );

        matrixStack.push();
        Vec3d cam = context.camera().getPos();
        matrixStack.translate(-cam.x, -cam.y, -cam.z);

        for (BlockPos pos : DISPLAYED_POS) {
            Box box = new Box(pos);
            WorldRenderer.renderFilledBox(matrixStack, consumers.getBuffer(NO_DEPTH),
                    box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
                    0.2f, 0.6f, 0.3f, MathHelper.lerp(normalizedAlphaPulse, 0f, 0.6f));
        }
        matrixStack.pop();
    }
}
