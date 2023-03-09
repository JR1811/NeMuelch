package net.shirojr.nemuelch.item.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolTankItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WandOfSolTankItemModel extends AnimatedGeoModel<WandOfSolTankItem> {
    @Override
    public Identifier getModelLocation(WandOfSolTankItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/wandofsol_tank.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WandOfSolTankItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/stations/wandofsol.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WandOfSolTankItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/wandofsol.animation.json");
    }
}
