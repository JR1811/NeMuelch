package net.shirojr.nemuelch.entity.Client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.JuggernautEntity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class JuggernautRenderer extends GeoEntityRenderer<JuggernautEntity> {
    public JuggernautRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new JuggernautModel());
    }

    @Override
    public Identifier getTextureLocation(JuggernautEntity instance) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/juggernaut/juggernaut.png");
    }
}
