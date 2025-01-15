package net.shirojr.nemuelch.item.client;

import net.shirojr.nemuelch.item.custom.gloveItem.TrainingGloveItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class TraininggloveRenderer extends GeoItemRenderer<TrainingGloveItem> {
    public TraininggloveRenderer() {
        super(new TraininggloveModel());
    }
}
