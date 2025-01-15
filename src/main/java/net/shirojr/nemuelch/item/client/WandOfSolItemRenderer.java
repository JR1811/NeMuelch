package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class WandOfSolItemRenderer extends GeoItemRenderer<WandOfSolItem> {
    public WandOfSolItemRenderer() {
        super(new WandOfSolItemModel());
    }
}
