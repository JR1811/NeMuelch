package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.caneItem.GladiusBladeItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GladiusBladeModel extends AnimatedGeoModel<GladiusBladeItem> {
    @Override
    public Identifier getModelLocation(GladiusBladeItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/gladiuscane_blade.geo.json");
    }

    @Override
    public Identifier getTextureLocation(GladiusBladeItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/gladiuscane.png");
    }

    @Override
    public Identifier getAnimationFileLocation(GladiusBladeItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/gladiuscane_blade.animation.json");
    }
}
