package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FallenGuardArmorSetItem;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class FallenGuardArmorRenderer extends GeoArmorRenderer<FallenGuardArmorSetItem> {
    public FallenGuardArmorRenderer() {
        super(new FallenGuardArmorModel());

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

