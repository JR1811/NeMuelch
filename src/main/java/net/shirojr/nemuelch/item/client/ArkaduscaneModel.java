package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.caneItem.ArkaduscaneItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ArkaduscaneModel extends AnimatedGeoModel<ArkaduscaneItem> {
    @Override
    public Identifier getModelLocation(ArkaduscaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/arkaduscane.geo.json");
    }

    @Override
    public Identifier getTextureLocation(ArkaduscaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/arkaduscane.png");
    }

    @Override
    public Identifier getAnimationFileLocation(ArkaduscaneItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/arkaduscane.animation.json");
    }
}
