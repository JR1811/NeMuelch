package net.shirojr.nemuelch.block.entity.client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.entity.WandOfSolBlockEntity;
import net.shirojr.nemuelch.item.custom.supportItem.WandOfSolItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WandOfSolBlockModel extends AnimatedGeoModel<WandOfSolBlockEntity> {
    @Override
    public Identifier getModelLocation(WandOfSolBlockEntity object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/wandofsol.geo.json");
    }

    @Override
    public Identifier getTextureLocation(WandOfSolBlockEntity object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/stations/wandofsol.png");
    }

    @Override
    public Identifier getAnimationFileLocation(WandOfSolBlockEntity animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/wandofsol.animation.json");
    }
}
