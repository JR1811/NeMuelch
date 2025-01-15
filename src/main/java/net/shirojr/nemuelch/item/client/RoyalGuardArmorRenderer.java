package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.armorAndShieldItem.RoyalGuardArmorSetItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class RoyalGuardArmorRenderer extends GeoArmorRenderer<RoyalGuardArmorSetItem> {
    public RoyalGuardArmorRenderer() {
        super(new RoyalGuardArmorModel());

        this.headBone = "armorHead";
        this.bodyBone = "armorBody";
        this.rightArmBone = "armorRightArm";
        this.leftArmBone = "armorLeftArm";
        this.rightLegBone = "armorRightLeg";
        this.leftLegBone = "armorLeftLeg";
        this.rightBootBone = "armorRightBoot";
        this.leftBootBone = "armorLeftBoot";
    }
}

