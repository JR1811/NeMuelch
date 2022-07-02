package net.shirojr.nemuelch.item.client;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.PestItem.PestCaneItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PestCaneModel extends AnimatedGeoModel<PestCaneItem> {
    @Override
    public Identifier getModelLocation(PestCaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/pestcane_noplant_v1.geo.json");
    }

    @Override
    public Identifier getTextureLocation(PestCaneItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/item/pestcane_noplant_v1.png");
    }

    @Override
    public Identifier getAnimationFileLocation(PestCaneItem animatable) {

        return null;
        //TODO: add custom animations from Blockbench

        // return new Identifier(NeMuelch.MOD_ID, "geo/pestcane_noplant_v1.geo.json");
        // add animation in "animations" folder (parent folder would be nemuelch)
    }
}
