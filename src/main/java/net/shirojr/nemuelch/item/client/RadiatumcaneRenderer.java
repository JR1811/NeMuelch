package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.caneItem.RadiatumCaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class RadiatumcaneRenderer extends GeoItemRenderer<RadiatumCaneItem> {
    public RadiatumcaneRenderer() {
        super(new RadiatumcaneModel());
    }
}
