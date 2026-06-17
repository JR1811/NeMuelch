package net.shirojr.nemuelch.block.entity.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AdvancedFogBlockEntityRenderer implements BlockEntityRenderer<AdvancedFogBlockEntity> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final BlockEntityRendererFactory.Context context;

    public AdvancedFogBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        context = ctx;
    }

    @Override
    public void render(AdvancedFogBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        handleFaceRendering(matrices, blockEntity.getData());
        handleDebugLineRendering(matrices, vertexConsumers, blockEntity);
    }

    private static void handleDebugLineRendering(MatrixStack matrices, VertexConsumerProvider vertexConsumers, AdvancedFogBlockEntity blockEntity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        if (!client.player.getMainHandStack().isOf(blockEntity.getCachedState().getBlock().asItem())) return;

        AdvancedFogBlockEntity.Data data = blockEntity.getData();
        Vector3f centerPos = new Vector3f(0.5f);
        Box renderedFaces = data.box();

        float minX = (float) renderedFaces.minX;
        float minY = (float) renderedFaces.minY;
        float minZ = (float) renderedFaces.minZ;
        float maxX = (float) renderedFaces.maxX;
        float maxY = (float) renderedFaces.maxY;
        float maxZ = (float) renderedFaces.maxZ;

        matrices.push();

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        vertexConsumer.vertex(positionMatrix, minX, minY, minZ).color(0.0F, 0.0F, 0.0F, 0.5F).normal(0, 1, 0).next();
        vertexConsumer.vertex(positionMatrix, centerPos.x, centerPos.y, centerPos.z).color(0.0F, 0.0F, 0.0F, 0.5F).normal(0, 1, 0).next();

        vertexConsumer.vertex(positionMatrix, maxX, maxY, maxZ).color(0.0F, 0.0F, 0.0F, 0.5F).normal(0, 1, 0).next();
        vertexConsumer.vertex(positionMatrix, centerPos.x, centerPos.y, centerPos.z).color(0.0F, 0.0F, 0.0F, 0.5F).normal(0, 1, 0).next();

        matrices.pop();
    }

    public static void handleFaceRendering(MatrixStack matrices, AdvancedFogBlockEntity.Data data) {
        Box renderedFaces = data.box();
        float red = data.getRed();
        float green = data.getGreen();
        float blue = data.getBlue();
        float alpha = data.getAlpha();

        float minX = (float) renderedFaces.minX;
        float minY = (float) renderedFaces.minY;
        float minZ = (float) renderedFaces.minZ;
        float maxX = (float) renderedFaces.maxX;
        float maxY = (float) renderedFaces.maxY;
        float maxZ = (float) renderedFaces.maxZ;

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.polygonOffset(-1.0F, -10.0F); // vanilla values
        RenderSystem.enablePolygonOffset();

        BufferBuilder bufferBuilder = new BufferBuilder(400);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        matrices.push();

        renderQuad(bufferBuilder, positionMatrix,
                minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ,
                red, green, blue, alpha);

        renderQuad(bufferBuilder, positionMatrix,
                minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ,
                red, green, blue, alpha);

        renderQuad(bufferBuilder, positionMatrix,
                minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ,
                red, green, blue, alpha);

        renderQuad(bufferBuilder, positionMatrix,
                minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                red, green, blue, alpha);

        renderQuad(bufferBuilder, positionMatrix,
                minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ,
                red, green, blue, alpha);

        renderQuad(bufferBuilder, positionMatrix,
                maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                red, green, blue, alpha);

        matrices.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disablePolygonOffset();
    }

    public static void renderQuad(VertexConsumer vertexConsumer, Matrix4f positionMatrix,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float x3, float y3, float z3,
                                  float x4, float y4, float z4,
                                  float r, float g, float b, float a) {
        vertexConsumer.vertex(positionMatrix, x1, y1, z1).color(r, g, b, a).next();
        vertexConsumer.vertex(positionMatrix, x2, y2, z2).color(r, g, b, a).next();
        vertexConsumer.vertex(positionMatrix, x3, y3, z3).color(r, g, b, a).next();
        vertexConsumer.vertex(positionMatrix, x4, y4, z4).color(r, g, b, a).next();
    }

    @Override
    public int getRenderDistance() {
        return 1024;
    }

    @Override
    public boolean rendersOutsideBoundingBox(AdvancedFogBlockEntity blockEntity) {
        return true;
    }
}
