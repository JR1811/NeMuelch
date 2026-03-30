package net.shirojr.nemuelch.block.entity.client;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.block.entity.custom.WaterCrateBlockEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class WaterCrateBlockEntityRenderer implements BlockEntityRenderer<WaterCrateBlockEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final MinecraftClient client;
    private final Map<BlockPos, Entity> entityCache = new HashMap<>();

    @SuppressWarnings("unused")
    public WaterCrateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.client = MinecraftClient.getInstance();
        this.entityRenderDispatcher = client.getEntityRenderDispatcher();
    }

    @Override
    public void render(WaterCrateBlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ClientWorld world = client.world;
        if (world == null) return;
        SingleVariantStorage<FluidVariant> fluidStorage = blockEntity.getFluidStorage();
        FluidVariant variant = fluidStorage.variant;
        float normalizedAmount = (float) fluidStorage.amount / WaterCrateBlockEntity.MAX_CAPACITY;

        if (!variant.isBlank() && normalizedAmount > 0) {
            int lightAbove = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos().up());
            this.renderFluid(blockEntity, normalizedAmount, matrices, vertexConsumers, lightAbove, overlay);
        }

        this.updateStoredEntityCache(blockEntity, world);
        Entity entity = this.entityCache.get(blockEntity.getPos());
        if (entity != null) {
            this.renderStoredEntity(entity, blockEntity.getStoredEntityDuration(), matrices, vertexConsumers, light, tickDelta);
        }
    }

    private void renderFluid(WaterCrateBlockEntity blockEntity, float normalizedAmount, MatrixStack matrices,
                             VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SingleVariantStorage<FluidVariant> fluidStorage = blockEntity.getFluidStorage();
        if (client == null || fluidStorage.amount <= 0 || fluidStorage.isResourceBlank()) return;

        Fluid fluid = fluidStorage.variant.getFluid();
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (handler == null) return;
        int color = handler.getFluidColor(client.world, blockEntity.getPos(), fluid.getDefaultState());
        Sprite[] fluidSprites = handler.getFluidSprites(client.world, blockEntity.getPos(), fluid.getDefaultState());
        Sprite stillSprite = fluidSprites[0];

        float height = MathHelper.lerp(normalizedAmount, 0.3f, 0.9f);

        float minX = 0.1f;
        float maxX = 0.9f;
        float minZ = 0.1f;
        float maxZ = 0.9f;

        float u0 = stillSprite.getFrameU(0);
        float u1 = stillSprite.getFrameU(stillSprite.getContents().getWidth());
        float v0 = stillSprite.getFrameV(0);
        float v1 = stillSprite.getFrameV(stillSprite.getContents().getHeight());

        float uScale = (maxX - minX);
        float vScale = (maxZ - minZ);
        u1 = u0 + (u1 - u0) * uScale;
        v1 = v0 + (v1 - v0) * vScale;

        matrices.push();

        Vector3f normalizedColor = new Vector3f(
                ((color >> 16) & 0xFF) / 255f,
                ((color >> 8) & 0xFF) / 255f,
                (color & 0xFF) / 255f
        );
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();

        vertexConsumer.vertex(positionMatrix, minX, height, minZ)
                .color(normalizedColor.x, normalizedColor.y, normalizedColor.z, 1f)
                .texture(u0, v0)
                .overlay(overlay)
                .light(light)
                .normal(normalMatrix, 0, 1, 0)
                .next();

        vertexConsumer.vertex(positionMatrix, minX, height, maxZ)
                .color(normalizedColor.x, normalizedColor.y, normalizedColor.z, 1f)
                .texture(u0, v1)
                .overlay(overlay)
                .light(light)
                .normal(normalMatrix, 0, 1, 0)
                .next();

        vertexConsumer.vertex(positionMatrix, maxX, height, maxZ)
                .color(normalizedColor.x, normalizedColor.y, normalizedColor.z, 1f)
                .texture(u1, v1)
                .overlay(overlay)
                .light(light)
                .normal(normalMatrix, 0, 1, 0)
                .next();

        vertexConsumer.vertex(positionMatrix, maxX, height, minZ)
                .color(normalizedColor.x, normalizedColor.y, normalizedColor.z, 1f)
                .texture(u1, v0)
                .overlay(overlay)
                .light(light)
                .normal(normalMatrix, 0, 1, 0)
                .next();

        matrices.pop();
    }

    private void renderStoredEntity(Entity storedEntity, long storedDuration, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        matrices.push();
        matrices.translate(0.5, 0.1, 0.5);

        float scale = 0.4f;
        matrices.scale(scale, scale, scale);
        double smoothTime = (storedDuration + tickDelta) * 0.2;
        float rotation = (float) (smoothTime % 360);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
        this.entityRenderDispatcher.render(storedEntity, 0, 0, 0, 0f, 1f, matrices, vertexConsumers, light);

        matrices.pop();
    }

    private void updateStoredEntityCache(WaterCrateBlockEntity blockEntity, ClientWorld world) {
        BlockPos pos = blockEntity.getPos();
        if (blockEntity.getStoredEntity() == null) {
            this.entityCache.remove(pos);
            return;
        }
        Entity cached = this.entityCache.get(pos);
        if (cached != null && cached.getType() == blockEntity.getStoredEntity().type()) {
            return;
        }
        Entity fresh = blockEntity.getStoredEntity().getEntity(world);
        if (fresh == null) this.entityCache.remove(pos);
        else this.entityCache.put(pos, fresh);
    }
}
