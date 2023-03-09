package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolPoleItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class WandOfSolPoleItemRenderer extends GeoItemRenderer<WandOfSolPoleItem> {
    public WandOfSolPoleItemRenderer() {
        super(new WandOfSolPoleItemModel());
    }
}
