package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class OnionRenderer extends GeoEntityRenderer<OnionEntity> {
    public OnionRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new OnionModel());
        this.shadowRadius = 0.6f;
    }


    @Override
    public Identifier getTextureLocation(OnionEntity instance) {    //TODO: might be wrong, idk yet...
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/onion/nemuelch-onion.png");
    }

    @Override
    public RenderLayer getRenderType(OnionEntity animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        stack.scale(0.8f, 0.8f, 0.8f);

        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}