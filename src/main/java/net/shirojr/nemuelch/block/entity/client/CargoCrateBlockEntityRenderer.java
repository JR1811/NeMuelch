package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.storage.CargoCrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchEntityModelLayers;

public class CargoCrateBlockEntityRenderer implements BlockEntityRenderer<CargoCrateBlockEntity> {
    private static final Identifier TEXTURE = NeMuelch.getId("textures/entity/cargo_crate.png");
    private final ModelPart model;

    public CargoCrateBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.model = ctx.getLayerModelPart(NeMuelchEntityModelLayers.CARGO_CRATE_BLOCK_ENTITY);
    }

    @Override
    public void render(CargoCrateBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = blockEntity.getWorld();
        if (world == null) return;
        BlockPos corePos = blockEntity.getPos();
        Direction facing = blockEntity.getCachedState().get(CargoCrateBlock.FACING).getOpposite();
        if (facing == Direction.UP) facing = Direction.DOWN;

        int blockLight = 0, skyLight = 0;
        for (Direction offsetDirection : Direction.values()) {
            BlockPos offsetPos = corePos.offset(offsetDirection, 2);
            int lightMapCoordinates = WorldRenderer.getLightmapCoordinates(world, offsetPos);
            blockLight = Math.max(blockLight, LightmapTextureManager.getBlockLightCoordinates(lightMapCoordinates));
            skyLight = Math.max(skyLight, LightmapTextureManager.getSkyLightCoordinates(lightMapCoordinates));
        }
        int packedMaxLight = LightmapTextureManager.pack(blockLight, skyLight);

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(facing.getRotationQuaternion());

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
        this.model.render(matrices, consumer, packedMaxLight, overlay);

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(CargoCrateBlockEntity blockEntity) {
        return true;
    }

    @SuppressWarnings("unused")
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData cube_r1 = base.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0F, -24.0F, -24.0F, 48.0F, 48.0F, 48.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -24.0F, 0.0F, 1.5708F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 256, 256);
    }
}
