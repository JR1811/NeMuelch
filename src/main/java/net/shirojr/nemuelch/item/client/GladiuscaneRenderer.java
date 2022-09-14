package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.caneItem.GladiuscaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class GladiuscaneRenderer extends GeoItemRenderer<GladiuscaneItem> {
    public GladiuscaneRenderer() {
        super(new GladiuscaneModel());
    }
}
