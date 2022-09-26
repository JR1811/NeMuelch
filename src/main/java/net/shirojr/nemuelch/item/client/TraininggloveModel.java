package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.gloveItem.TraininggloveItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TraininggloveModel extends AnimatedGeoModel<TraininggloveItem> {
    @Override
    public Identifier getModelLocation(TraininggloveItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/trainingglove.geo.json");
    }

    @Override
    public Identifier getTextureLocation(TraininggloveItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/training_glove.png");
    }

    @Override
    public Identifier getAnimationFileLocation(TraininggloveItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/trainingglove.animation.json");
    }
}
