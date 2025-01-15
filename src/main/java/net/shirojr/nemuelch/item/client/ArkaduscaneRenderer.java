package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.caneItem.ArkaduscaneItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ArkaduscaneRenderer extends GeoItemRenderer<ArkaduscaneItem> {
    public ArkaduscaneRenderer() {
        super(new ArkaduscaneModel());
    }
}
