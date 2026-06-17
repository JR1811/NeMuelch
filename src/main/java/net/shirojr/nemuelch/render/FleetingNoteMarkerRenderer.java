package net.shirojr.nemuelch.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.implementation.FleetingNotesComponent;
import net.shirojr.nemuelch.compat.cca.util.FleetingNoteData;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Optional;

public class FleetingNoteMarkerRenderer implements WorldRenderEvents.AfterTranslucent {
    private static final Identifier MARKER_IMAGE = NeMuelch.getId("textures/misc/note_marker.png");
    private static final float SIZE = NeMuelchConfigInit.CONFIG.fleetingNotes.getSpriteSize();
    private final int MARKER_COLOR;

    public FleetingNoteMarkerRenderer() {
        try {
            String spriteColorArgb = Optional.ofNullable(NeMuelchConfigInit.CONFIG.fleetingNotes.getSpriteColorArgb()).orElse("0xFFFFFFFF");
            this.MARKER_COLOR = (int) Long.parseLong(spriteColorArgb.replace("0x", "").replace("#", ""), 16);
        } catch (NumberFormatException e) {
            NeMuelch.LOGGER.error("Invalid ARGB color hex code for Fleeting Note in config", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterTranslucent(WorldRenderContext context) {
        if (SIZE <= 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) return;
        FleetingNotesComponent component = FleetingNotesComponent.get(world);
        if (component.isEmpty() || !component.isAnyInRenderDistance(player.getPos())) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();
        Quaternionf camRotation = camera.getRotation();
        MatrixStack matrixStack = context.matrixStack();

        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(MARKER_IMAGE));

        for (FleetingNoteData.Positioned fleetingNote : component.getUnsyncedData()) {
            Vec3d pos = fleetingNote.pos();
            if (fleetingNote.isOutsideOfRenderDistance(pos)) continue;

            matrixStack.push();

            matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
            matrixStack.translate(pos.x, pos.y, pos.z);
            matrixStack.multiply(camRotation);
            matrixStack.scale(SIZE, SIZE, SIZE);

            Matrix4f posMatrix = matrixStack.peek().getPositionMatrix();

            buffer.vertex(posMatrix, -1f, 1f, 0f).color(MARKER_COLOR).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 0f, 1f).next();
            buffer.vertex(posMatrix, -1f, -1f, 0f).color(MARKER_COLOR).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 0f, 1f).next();
            buffer.vertex(posMatrix, 1f, -1f, 0f).color(MARKER_COLOR).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 0f, 1f).next();
            buffer.vertex(posMatrix, 1f, 1f, 0f).color(MARKER_COLOR).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0f, 0f, 1f).next();

            matrixStack.pop();
        }

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }
}
