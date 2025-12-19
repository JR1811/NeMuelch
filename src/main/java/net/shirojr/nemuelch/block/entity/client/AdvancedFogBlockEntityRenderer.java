package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import org.joml.Matrix4f;

public class AdvancedFogBlockEntityRenderer implements BlockEntityRenderer<AdvancedFogBlockEntity> {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final BlockEntityRendererFactory.Context context;

    private static final RenderLayer RENDER_LAYER = RenderLayer.of(
            "fog_translucent",
            VertexFormats.POSITION_COLOR,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.COLOR_PROGRAM) // Changed from TRANSLUCENT_PROGRAM
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .writeMaskState(RenderPhase.ALL_MASK)
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .build(false)
    );

    public AdvancedFogBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        context = ctx;
    }

    @Override
    public void render(AdvancedFogBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Box renderedFaces = blockEntity.getRenderedFaces();
        float red = blockEntity.getRed();
        float green = blockEntity.getGreen();
        float blue = blockEntity.getBlue();
        float alpha = blockEntity.getAlpha();

        float minX = (float) renderedFaces.minX;
        float minY = (float) renderedFaces.minY;
        float minZ = (float) renderedFaces.minZ;
        float maxX = (float) renderedFaces.maxX;
        float maxY = (float) renderedFaces.maxY;
        float maxZ = (float) renderedFaces.maxZ;

        matrices.push();

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RENDER_LAYER);


        renderQuad(vertexConsumer, positionMatrix,
                minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ,
                red, green, blue, alpha);

        renderQuad(vertexConsumer, positionMatrix,
                minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ,
                red, green, blue, alpha);

        renderQuad(vertexConsumer, positionMatrix,
                minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ,
                red, green, blue, alpha);

        renderQuad(vertexConsumer, positionMatrix,
                minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                red, green, blue, alpha);

        renderQuad(vertexConsumer, positionMatrix,
                minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ,
                red, green, blue, alpha);

        renderQuad(vertexConsumer, positionMatrix,
                maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                red, green, blue, alpha);

        matrices.pop();
    }

    private void renderQuad(VertexConsumer vertexConsumer, Matrix4f positionMatrix,
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
        return 256;
    }
}
