package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.armorAndShieldItem.FortifiedShieldItem;
import net.shirojr.nemuelch.item.custom.caneItem.ArkaduscaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class FortifiedShieldRenderer extends GeoItemRenderer<FortifiedShieldItem> {
    public FortifiedShieldRenderer() {
        super(new FortifiedShieldModel());
    }
}
