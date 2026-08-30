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
import net.shirojr.nemuelch.compat.cca.implementation.ClimbingPreventionZoneComponent;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.ComplexZone;
import net.shirojr.nemuelch.mixin.access.DebugRendererAccess;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ZonesDebugRenderer implements WorldRenderEvents.DebugRender {
    @Override
    public void beforeDebugRender(WorldRenderContext context) {
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        MatrixStack matrices = context.matrixStack();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.debugRenderer == null) return;
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        DebugRendererAccess debugRenderer = (DebugRendererAccess) client.debugRenderer;
        if (player == null || world == null || !debugRenderer.showChunkBorder()) return;

        NotificationZoneComponent notificationZoneComponent = NotificationZoneComponent.get(world);
        this.renderZones(client, notificationZoneComponent.getListenedNotificationZones(player.getUuid()),
                player, context, consumers, matrices, new Vector4f(0.9f, 0.3f, 0.2f, 1f));

        ClimbingPreventionZoneComponent climbingPreventionZoneComponent = ClimbingPreventionZoneComponent.get(world);
        this.renderZones(client, climbingPreventionZoneComponent.getZones(), player, context, consumers,
                matrices, new Vector4f(0.5f, 0.3f, 0.5f, 1f));
    }

    private void renderZones(MinecraftClient client, Set<ComplexZone> zones, ClientPlayerEntity player,
                             WorldRenderContext context, VertexConsumerProvider consumers, MatrixStack matrices, Vector4f color) {
        for (ComplexZone zone : zones) {
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

            matrices.push();
            matrices.translate(camPos.x, camPos.y, camPos.z);
            edges.forEach(edge -> {
                MatrixStack.Entry peek = matrices.peek();
                Matrix4f positionMatrix = peek.getPositionMatrix();
                Vec3d firstVertex = edge.firstVertex;
                Vec3d secondVertex = edge.secondVertex;
                debugLineConsumer.vertex(positionMatrix, (float) firstVertex.x, (float) firstVertex.y, (float) firstVertex.z)
                        .color(color.x, color.y, color.z, color.w)
                        .next();
                debugLineConsumer.vertex(positionMatrix, (float) secondVertex.x, (float) secondVertex.y, (float) secondVertex.z)
                        .color(color.x, color.y, color.z, color.w)
                        .next();
            });
            matrices.pop();
            matrices.push();
            for (int i = 0; i < originalVertices.size(); i++) {
                Vec3d vertex = originalVertices.get(i);
                DebugRenderer.drawString(matrices, consumers, "Vertex Index: " + i, vertex.x, vertex.y, vertex.z, -1, 0.01f, true, 0, true);
            }
            matrices.pop();
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
