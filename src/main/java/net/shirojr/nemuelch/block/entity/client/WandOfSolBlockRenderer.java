package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.block.entity.WandOfSolBlockEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class WandOfSolBlockRenderer extends GeoBlockRenderer<WandOfSolBlockEntity> {
    public WandOfSolBlockRenderer(BlockEntityRendererFactory.Context context) {
        super(new WandOfSolBlockModel());
    }

    @Override
    public RenderLayer getRenderType(WandOfSolBlockEntity animatable, float partialTicks,
                                     MatrixStack stack, VertexConsumerProvider renderTypeBuffer,
                                     VertexConsumer vertexBuilder, int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable), true);
    }
}
