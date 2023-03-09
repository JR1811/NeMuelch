package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolPoleItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WandOfSolPoleItemModel extends AnimatedGeoModel<WandOfSolPoleItem> {
    @Override
    public Identifier getModelLocation(WandOfSolPoleItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/wandofsol_pole.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WandOfSolPoleItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/stations/wandofsol.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WandOfSolPoleItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/wandofsol.animation.json");
    }
}
