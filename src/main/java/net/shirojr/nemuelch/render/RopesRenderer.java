package net.shirojr.nemuelch.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import net.shirojr.nemuelch.item.client.RopeModificationHandler;
import net.shirojr.nemuelch.item.custom.adminToolItem.RopeModifierItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;

public class RopesRenderer implements WorldRenderEvents.AfterEntities {
    @Override
    public void afterEntities(WorldRenderContext context) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) return;
        ClientWorld world = context.world();
        RopesComponent ropesComponent = RopesComponent.get(world);
        if (ropesComponent.isEmpty()) return;
        Camera camera = context.camera();
        Vec3d cameraPos = camera.getPos();
        Optional<RopeData> hoveredRope = Optional.empty();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getMainHandStack().getItem() instanceof RopeModifierItem) {
            hoveredRope = RopeModificationHandler.getHoveredRope(world, camera);
        }
        matrices.push();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        for (RopeData rope : ropesComponent.getRopes()) {
            if (rope.isUnloaded(world)) continue;
            boolean isHovered = hoveredRope.map(entry -> entry.equals(rope)).orElse(false);
            this.renderRope(rope, matrices, consumers, isHovered);
        }
        matrices.pop();
    }

    private void renderRope(RopeData rope, MatrixStack matrices, VertexConsumerProvider consumers, boolean isHovered) {
        matrices.push();
        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayer.getLeash());
        Vec3d posA = rope.pointA();
        Vec3d posB = rope.pointB();
        Vec3d ropeVec = posB.subtract(posA);
        int segments = rope.segments();
        float width = rope.width();
        float slack = rope.slack();
        double invHorizontalLength = MathHelper.inverseSqrt(ropeVec.x * ropeVec.x + ropeVec.z * ropeVec.z) * width / 2.0F;
        double normalX = ropeVec.z * invHorizontalLength;
        double normalZ = ropeVec.x * invHorizontalLength;

        matrices.translate(posA.x, posA.y, posA.z);
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();

        for (int segmentIndex = 0; segmentIndex <= segments; segmentIndex++) {
            this.renderRopePiece(vertexConsumer, positionMatrix, ropeVec, width, width, normalX, normalZ, segmentIndex, segments, slack, isHovered);
        }

        for (int segmentIndex = segments; segmentIndex >= 0; segmentIndex--) {
            this.renderRopePiece(vertexConsumer, positionMatrix, ropeVec, width, 0.0F, normalX, normalZ, segmentIndex, segments, slack, isHovered);
        }
        matrices.pop();
    }


    private void renderRopePiece(
            VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d delta,
            float topWidth, float bottomWidth, double normalX, double normalZ, int segment, int maxSegments, float slack, boolean isHovered
    ) {
        float segmentProgress = (float) segment / maxSegments;
        int packedLight = LightmapTextureManager.pack(15, 15);
        float colorMultiplier = segment % 2 == 1 ? 0.7F : 1.0F;
        Vector3f color = isHovered ? new Vector3f(1f, 1f, 1f) : new Vector3f(0.5F * colorMultiplier, 0.4F * colorMultiplier, 0.3F * colorMultiplier);

        double sag = slack * segmentProgress * (segmentProgress - 1.0F);

        double x = delta.x * segmentProgress;
        double y = delta.y > 0 ?
                delta.y * segmentProgress * segmentProgress :
                delta.y - delta.y * (1.0F - segmentProgress) * (1.0F - segmentProgress);
        y += sag;
        double z = delta.z * segmentProgress;
        vertexConsumer.vertex(positionMatrix,
                        (float) (x - normalX),
                        (float) (y + bottomWidth),
                        (float) (z + normalZ))
                .color(color.x, color.y, color.z, 1.0F)
                .light(packedLight)
                .next();
        vertexConsumer.vertex(positionMatrix,
                        (float) (x + normalX),
                        (float) (y + topWidth - bottomWidth),
                        (float) (z - normalZ)).color(color.x, color.y, color.z, 1.0F)
                .light(packedLight)
                .next();
    }
}
