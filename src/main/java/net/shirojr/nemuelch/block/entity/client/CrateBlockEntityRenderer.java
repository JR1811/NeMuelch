package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchProperties;

public class CrateBlockEntityRenderer implements BlockEntityRenderer<CrateBlockEntity> {
    private final BlockEntityRendererFactory.Context ctx;

    public CrateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void render(CrateBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ItemRenderer itemRenderer = client.getItemRenderer();
        Direction direction = entity.getCachedState().get(CrateBlock.FACING);

        DefaultedList<ItemStack> bottomInvStacks = entity.getBottomInventory().stacks;
        for (int i = 0; i < bottomInvStacks.size(); i++) {
            ItemStack entryStack = bottomInvStacks.get(i);
            Direction direction2 = Direction.fromHorizontal((i + direction.getHorizontal()) % 4);
            float g = -direction2.asRotation();

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            matrices.translate(i * 0.2, 0.5, i * 0.2);
            itemRenderer.renderItem(entryStack, ModelTransformationMode.GUI, light, overlay, matrices, vertexConsumers,
                    entity.getWorld(), (int) (entity.getPos().asLong() + i));
            matrices.pop();
        }

        if (entity.getCachedState().get(NeMuelchProperties.CRATE_TYPE) != CrateBlock.Type.DOUBLE) return;
    }
}
