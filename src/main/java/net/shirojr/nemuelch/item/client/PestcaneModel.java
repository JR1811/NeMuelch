package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.PestcaneItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PestcaneModel extends AnimatedGeoModel<PestcaneItem> {
    @Override
    public Identifier getModelLocation(PestcaneItem object) {
        NeMuelch.LOGGER.info("accessing geo.json data in PestcaneModel.java class");
        return new Identifier(NeMuelch.MOD_ID, "geo/pestcane.geo.json");
    }

    @Override
    public Identifier getTextureLocation(PestcaneItem object) {
        NeMuelch.LOGGER.info("accessing .png texture data in PestcaneModel.java class");
        return new Identifier(NeMuelch.MOD_ID, "textures/item/pestcane.png");
    }

    @Override
    public Identifier getAnimationFileLocation(PestcaneItem animatable) {
        NeMuelch.LOGGER.info("accessing animation.json data in PestcaneModel.java class");
        return new Identifier(NeMuelch.MOD_ID, "animations/pestcane.animation.json");
    }
}
