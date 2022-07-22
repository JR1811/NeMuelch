package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.PestcaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class PestcaneRenderer extends GeoItemRenderer<PestcaneItem> {
    public PestcaneRenderer() {
        super(new PestcaneModel());
    }
}
