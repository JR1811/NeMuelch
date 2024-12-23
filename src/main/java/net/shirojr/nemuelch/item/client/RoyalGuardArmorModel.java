package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.RoyalGuardArmorSetItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RoyalGuardArmorModel extends AnimatedGeoModel<RoyalGuardArmorSetItem> {
    @Override
    public Identifier getModelLocation(RoyalGuardArmorSetItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/royal_guard_armor.geo.json");
    }

    @Override
    public Identifier getTextureLocation(RoyalGuardArmorSetItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/models/armor/royal_guard_armor.png");
    }

    @Override
    public Identifier getAnimationFileLocation(RoyalGuardArmorSetItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/blank.animation.json");
    }
}
