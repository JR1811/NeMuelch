package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.caneItem.ArkaduscaneItem;
import net.shirojr.nemuelch.item.custom.caneItem.GladiusBladeItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class GladiusBladeRenderer extends GeoItemRenderer<GladiusBladeItem> {
    public GladiusBladeRenderer() {
        super(new GladiusBladeModel());
    }
}
