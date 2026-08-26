package net.shirojr.nemuelch.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.NotificationZone;
import net.shirojr.nemuelch.mixin.access.DebugRendererAccess;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class NotificationZoneDebugRenderer implements WorldRenderEvents.DebugRender {
    @Override
    public void beforeDebugRender(WorldRenderContext context) {
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        MatrixStack matrixStack = context.matrixStack();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.debugRenderer == null) return;
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        DebugRendererAccess debugRenderer = (DebugRendererAccess) client.debugRenderer;
        if (player == null || world == null || !debugRenderer.showChunkBorder()) return;

        NotificationZoneComponent component = NotificationZoneComponent.get(world);
        for (NotificationZone zone : component.getListenedNotificationZones(player.getUuid())) {
            Box boundingBox = zone.getBoundingBox();
            if (boundingBox == null || zone.isEmpty()) continue;

            float viewDistance = client.gameRenderer.getViewDistance();
            boolean anyVertexInDistance = false;
            List<Vec3d> originalVertices = zone.getVertices();
            for (Vec3d vertex : originalVertices) {
                if (player.squaredDistanceTo(vertex) <= viewDistance * viewDistance) {
                    anyVertexInDistance = true;
                    break;
                }
            }
            if (!anyVertexInDistance) continue;

            Vec3d camPos = context.camera().getPos().negate();
            VertexConsumer debugLineConsumer = consumers.getBuffer(RenderLayer.getDebugLineStrip(0.2));

            List<Vec3d> vertices = zone.getAllVertices(true, false);
            List<Vec3d> verticesFlipped = zone.getAllVertices(true, true);
            List<Edge> edges = new ArrayList<>(Edge.get(vertices));
            edges.addAll(Edge.get(verticesFlipped));

            matrixStack.push();
            matrixStack.translate(camPos.x, camPos.y, camPos.z);
            edges.forEach(edge -> {
                MatrixStack.Entry peek = matrixStack.peek();
                Matrix4f positionMatrix = peek.getPositionMatrix();
                Vec3d firstVertex = edge.firstVertex;
                Vec3d secondVertex = edge.secondVertex;
                debugLineConsumer.vertex(positionMatrix, (float) firstVertex.x, (float) firstVertex.y, (float) firstVertex.z)
                        .color(0.9f, 0.3f, 0.2f, 1f)
                        .next();
                debugLineConsumer.vertex(positionMatrix, (float) secondVertex.x, (float) secondVertex.y, (float) secondVertex.z)
                        .color(0.9f, 0.3f, 0.2f, 1f)
                        .next();
            });
            matrixStack.pop();
            matrixStack.push();
            for (int i = 0; i < originalVertices.size(); i++) {
                Vec3d vertex = originalVertices.get(i);
                DebugRenderer.drawString(matrixStack, consumers, "Vertex Index: " + i, vertex.x, vertex.y, vertex.z, -1, 0.01f, true, 0, true);
            }
            matrixStack.pop();
        }
    }

    private record Edge(Vec3d firstVertex, Vec3d secondVertex) {

        public static List<Edge> get(List<Vec3d> vertices) {
            if (vertices.size() < 2) throw new IllegalStateException("Edges need at least 2 vertices");
            List<Edge> result = new ArrayList<>();
            Vec3d previousVertex = null;
            for (Vec3d vertex : vertices) {
                if (previousVertex != null) {
                    result.add(new Edge(previousVertex, vertex));
                }
                previousVertex = vertex;
            }
            return result;
        }
    }
}
