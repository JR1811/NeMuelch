package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FortifiedShieldItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class FortifiedShieldModel extends AnimatedGeoModel<FortifiedShieldItem> {
    @Override
    public Identifier getModelLocation(FortifiedShieldItem object) {

        return new Identifier(NeMuelch.MOD_ID, "geo/fortifiedshield.geo.json");
    }

    @Override
    public Identifier getTextureLocation(FortifiedShieldItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/fortifiedshield.png");
    }

    @Override
    public Identifier getAnimationFileLocation(FortifiedShieldItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/fortifiedshield.animation.json");
    }
}
