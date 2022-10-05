package net.shirojr.nemuelch.entity.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class OnionModel extends AnimatedGeoModel<OnionEntity> {

    @Override
    public Identifier getModelLocation(OnionEntity object) {
        NeMuelch.LOGGER.info("retrieving model data (geo)");
        return new Identifier(NeMuelch.MOD_ID, "geo/onion.geo.json");
    }

    @Override
    public Identifier getTextureLocation(OnionEntity object) {
        NeMuelch.LOGGER.info("retrieving model data (geo)");
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/onion/nemuelch-onion.png");
    }

    @Override
    public Identifier getAnimationFileLocation(OnionEntity animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/onion.animation.json");
    }
}

