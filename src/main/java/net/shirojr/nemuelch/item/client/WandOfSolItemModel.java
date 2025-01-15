package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WandOfSolItemModel extends AnimatedGeoModel<WandOfSolItem> {
    @Override
    public Identifier getModelLocation(WandOfSolItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/wandofsol.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WandOfSolItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/stations/wandofsol.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WandOfSolItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/wandofsol.animation.json");
    }
}
