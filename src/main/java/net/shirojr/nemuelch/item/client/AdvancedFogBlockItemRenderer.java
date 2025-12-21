package net.shirojr.nemuelch.item.client;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.shirojr.nemuelch.block.entity.client.AdvancedFogBlockEntityRenderer;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.item.util.ThirdPersonInvisible;

public class AdvancedFogBlockItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer, ThirdPersonInvisible {
    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound blockEntityNbt = nbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY);
        AdvancedFogBlockEntity.Data data;
        if (blockEntityNbt.isEmpty()) {
            data = new AdvancedFogBlockEntity.Data();
        } else {
            data = AdvancedFogBlockEntity.Data.fromNbt(blockEntityNbt);
        }

        data = data.withBox(new Box(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));

        AdvancedFogBlockEntityRenderer.handleFaceRendering(matrices, vertexConsumers, data);
    }
}
