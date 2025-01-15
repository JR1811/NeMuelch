package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.caneItem.GladiuscaneItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GladiuscaneModel extends AnimatedGeoModel<GladiuscaneItem> {
    @Override
    public Identifier getModelLocation(GladiuscaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/gladiuscane_cane.geo.json");
    }

    @Override
    public Identifier getTextureLocation(GladiuscaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/gladiuscane.png");
    }

    @Override
    public Identifier getAnimationFileLocation(GladiuscaneItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/gladiuscane_cane.animation.json");
    }
}
