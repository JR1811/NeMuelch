package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.caneItem.RadiatumCaneItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RadiatumcaneModel extends AnimatedGeoModel<RadiatumCaneItem> {
    @Override
    public Identifier getModelLocation(RadiatumCaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/radiatumcane.geo.json");
    }

    @Override
    public Identifier getTextureLocation(RadiatumCaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/radiatumcane.png");
    }

    @Override
    public Identifier getAnimationFileLocation(RadiatumCaneItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/radiatumcane.animation.json");
    }
}
