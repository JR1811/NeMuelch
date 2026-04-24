package net.shirojr.nemuelch.item.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.weaponry.ChainedMaceItem;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;

import java.util.Optional;

public class ChainedMaceItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, ThirdPersonInvisible {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/entity/chained_mace.png");

    private ChainedMaceItemModel<PlayerEntity> model;

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Optional<Block> loadedBlock = ChainedMaceItem.getLoadedBlock(stack);
        if (loadedBlock.isEmpty()) return;
        if (model == null) {
            this.model = new ChainedMaceItemModel<>(ChainedMaceItemModel.getTexturedModelData().createModel());
        }

        matrices.push();
        matrices.translate(0, 1, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

        if (mode.isFirstPerson()) {
            matrices.translate(0, 0.3, 0);
        }
        if (mode.equals(ModelTransformationMode.GUI)) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(22.5f));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0));
        }

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        this.model.render(matrices, consumer, light, overlay, 1f, 1f, 1f, 1f);
        this.renderLoadedBlock(loadedBlock.get().asItem().getDefaultStack(), matrices, vertexConsumers, mode, light, overlay);
        matrices.pop();
    }

    private void renderLoadedBlock(ItemStack blockStack, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                   ModelTransformationMode mode, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        float scale = 1.6f;

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.translate(0, 1.73, 0.72);
        matrices.scale(scale, scale, scale);
        client.getItemRenderer().renderItem(blockStack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, client.world, client.player.getId());
        matrices.pop();
    }
}
