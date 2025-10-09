package net.shirojr.nemuelch.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.mixin.access.DebugRendererAccess;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class BlightDebugRenderer implements WorldRenderEvents.DebugRender {

    @Override
    public void beforeDebugRender(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.debugRenderer == null) return;
        ClientPlayerEntity player = client.player;
        DebugRendererAccess debugRenderer = (DebugRendererAccess) client.debugRenderer;
        if (player == null || !debugRenderer.showChunkBorder()) return;
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(
                player.getWorld().getChunk(player.getChunkPos().x, player.getChunkPos().z)
        );
        if (blightChunkComponent.isEmpty()) return;
        BlightChunkComponent component = blightChunkComponent.get();
        MatrixStack matrixStack = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();

        for (BlockPos posWithBlight : component.getPosWithBlights()) {
            List<BlightType> orderedList = BlightType.asOrderedList(component.getBlightsOfPos(posWithBlight));
            for (int i = 0; i < orderedList.size(); i++) {
                BlightType blockBlightType = orderedList.get(i);

                int color = blockBlightType.getDebugColor();
                float normalizedAlphaPulse = (MathHelper.sin((player.age + context.tickDelta()) * 0.1f) + 1f) * 0.5f;
                DebugRenderer.drawBox(
                        matrixStack, consumers,
                        posWithBlight, 0,
                        0.2f, 0.3f, 0.6f, MathHelper.lerp(normalizedAlphaPulse, 0f, 0.2f)
                );

                double verticalOffset = i - (orderedList.size() * 0.5);
                double stringY = posWithBlight.getY() + 0.5 + (verticalOffset * 0.2);
                DebugRenderer.drawString(
                        matrixStack, consumers, blockBlightType.asString(),
                        posWithBlight.getX() + 0.5, stringY, posWithBlight.getZ() + 0.5, color,
                        0.014f, true, 0f, true
                );
            }
        }

        EnumSet<BlightType> completeChunkBlights = component.getCompleteChunkBlights();
        ClientWorld world = client.world;
        if (!component.isEmpty() && world != null) {
            ChunkPos chunkPos = component.getProvider().getPos();
            int x = chunkPos.getCenterX();
            int z = chunkPos.getCenterZ();
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
            BlockPos displayPos = new BlockPos(x, y + 10, z);
            List<BlightType> orderedList = BlightType.asOrderedList(completeChunkBlights);
            for (int i = 0; i < orderedList.size(); i++) {
                BlightType blockBlightType = orderedList.get(i);
                int color = blockBlightType.getDebugColor();
                double verticalOffset = i - (orderedList.size() * 0.5);
                double stringY = displayPos.getY() + 0.5 + (verticalOffset * 0.2);
                DebugRenderer.drawString(
                        matrixStack, consumers, blockBlightType.asString(),
                        displayPos.getX() + 0.5, stringY, displayPos.getZ() + 0.5, color,
                        0.1f, true, 0f, true
                );
            }
        }
    }
}
