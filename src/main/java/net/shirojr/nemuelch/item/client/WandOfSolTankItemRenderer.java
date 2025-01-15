package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolTankItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class WandOfSolTankItemRenderer extends GeoItemRenderer<WandOfSolTankItem> {
    public WandOfSolTankItemRenderer() {
        super(new WandOfSolTankItemModel());
    }
}
