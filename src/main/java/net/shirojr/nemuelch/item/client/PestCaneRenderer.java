package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.PestItem.PestCaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class PestCaneRenderer extends GeoItemRenderer<PestCaneItem> {
    public PestCaneRenderer() {
        super(new PestCaneModel());
    }
}
