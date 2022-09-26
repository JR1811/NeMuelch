package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.gloveItem.TraininggloveItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class TraininggloveRenderer extends GeoItemRenderer<TraininggloveItem> {
    public TraininggloveRenderer() {
        super(new TraininggloveModel());
    }
}
