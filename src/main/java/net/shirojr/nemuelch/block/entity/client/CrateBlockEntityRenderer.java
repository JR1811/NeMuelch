package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchProperties;

import java.util.HashMap;
import java.util.Map;

public class CrateBlockEntityRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final Map<BlockPos, Entity> entityCache = new HashMap<>();

    @SuppressWarnings("unused")
    public CrateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        this.entityRenderDispatcher = client.getEntityRenderDispatcher();
        this.itemRenderer = client.getItemRenderer();
    }

    @Override
    public void render(CrateBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientWorld world = client.world;
        if (world == null) return;

        BlockState blockState = blockEntity.getCachedState();
        Direction direction = blockState.get(CrateBlock.FACING);
        CrateBlock.Type type = blockState.get(NeMuelchProperties.CRATE_TYPE);

        if (type == CrateBlock.Type.SINGLE || type == CrateBlock.Type.DOUBLE) {
            matrices.push();
            this.renderItemLayer(world, matrices, vertexConsumers, direction, blockEntity.getBottomInventory().stacks,
                    itemRenderer, light, overlay, blockEntity.getPos(), false);
            matrices.pop();

            SimpleInventory topInventory = blockEntity.getTopInventory();
            if (topInventory != null) {
                matrices.push();
                matrices.translate(0, 0.6, 0);
                this.renderItemLayer(world, matrices, vertexConsumers, direction, blockEntity.getTopInventory().stacks,
                        itemRenderer, light, overlay, blockEntity.getPos(), false);
                matrices.pop();
            }
        } else if (type == CrateBlock.Type.ANGLED) {
            matrices.push();
            this.renderItemLayer(world, matrices, vertexConsumers, direction, blockEntity.getBottomInventory().stacks,
                    itemRenderer, light, overlay, blockEntity.getPos(), true);
            matrices.pop();
        }

        this.updateStoredEntityCache(blockEntity, world);
        Entity entity = this.entityCache.get(blockEntity.getPos());
        if (entity != null) {
            this.renderStoredEntity(entity, blockEntity.getStoredEntityDuration(), matrices, vertexConsumers, light, tickDelta);
        }
    }

    private void renderItemLayer(World world, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                 Direction direction, DefaultedList<ItemStack> stacks,
                                 ItemRenderer itemRenderer, int light, int overlay, BlockPos seedPos, boolean isAngled) {
        float itemScale = 0.2f;
        int columns = 2;
        int rows = 3;
        float spacingX = 0.28f;
        float spacingZ = 0.28f;
        float totalSpaceX = (columns - 1) * spacingX;
        float totalSpaceZ = (rows - 1) * spacingZ;
        float startX = -totalSpaceX / 2f;
        float startZ = -totalSpaceZ / 2f;

        matrices.push();
        matrices.translate(0.5, 0.15, 0.5);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(direction.asRotation()));
        if (isAngled) {
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(22.5f));
            matrices.translate(0, 0.2, 0.025);
        }
        matrices.translate(0, 0, 0.56);

        for (int i = 0; i < stacks.size(); i++) {
            ItemStack entryStack = stacks.get(i);

            int column = i / 3;
            int row = i % 3;
            float x = startX + column * spacingX;
            float z = startZ - row * spacingZ;

            matrices.push();
            matrices.translate(x, 0, z);
            matrices.scale(itemScale, itemScale, itemScale);

            itemRenderer.renderItem(entryStack, ModelTransformationMode.NONE, light, overlay, matrices, vertexConsumers,
                    world, (int) (seedPos.asLong() + i));
            matrices.pop();
        }

        matrices.pop();
    }

    private void updateStoredEntityCache(CrateBlockEntity blockEntity, ClientWorld world) {
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
}
