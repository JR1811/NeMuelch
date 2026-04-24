package net.shirojr.nemuelch.entity.client.armor;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.shirojr.nemuelch.item.custom.weaponry.PortableBarrelItem;

public class PortableBarrelRenderer implements ArmorRenderer {
    private final MinecraftClient client;
    private final BlockState barrelState;

    public PortableBarrelRenderer(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, ItemStack stack,
                                  LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i,
                                  BipedEntityModel<LivingEntity> livingEntityBipedEntityModel) {
        client = MinecraftClient.getInstance();
        this.barrelState = Blocks.BARREL.getDefaultState();
    }


    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
        if (!(entity.getEquippedStack(ArmorItem.Type.CHESTPLATE.getEquipmentSlot()).getItem() instanceof PortableBarrelItem)) {
            return;
        }
        matrices.push();
        //TODO: translate / scale model
        client.getBlockRenderManager().renderBlockAsEntity(barrelState, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
