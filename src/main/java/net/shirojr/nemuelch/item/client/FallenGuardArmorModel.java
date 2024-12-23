package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FallenGuardArmorSetItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class FallenGuardArmorModel extends AnimatedGeoModel<FallenGuardArmorSetItem> {
    @Override
    public Identifier getModelLocation(FallenGuardArmorSetItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/fallen_guard_armor.geo.json");
    }

    @Override
    public Identifier getTextureLocation(FallenGuardArmorSetItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/models/armor/fallen_guard_armor.png");
    }

    @Override
    public Identifier getAnimationFileLocation(FallenGuardArmorSetItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/blank.animation.json");
    }
}
