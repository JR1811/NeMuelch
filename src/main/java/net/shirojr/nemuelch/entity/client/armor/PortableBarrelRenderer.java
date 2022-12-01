package net.shirojr.nemuelch.entity.client.armor;

import net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class PortableBarrelRenderer extends GeoArmorRenderer<PortableBarrelItem> {
    public PortableBarrelRenderer() {
        super(new PortableBarrelModel());

        this.headBone = "armorHead";
        this.bodyBone = "armorBody";
        this.rightArmBone = "armorRightArm";
        this.leftArmBone = "armorLeftArm";
        this.rightLegBone = "armorLeftLeg"; //FIXME: mby the other way around?
        this.leftLegBone = "armorRightLeg";
        this.rightBootBone = "armorRightBoot";
        this.leftBootBone = "armorLeftBoot";
    }
}
