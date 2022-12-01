package net.shirojr.nemuelch.entity.client.armor;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.custom.armorItem.PortableBarrelItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PortableBarrelModel extends AnimatedGeoModel<PortableBarrelItem> {
    @Override
    public Identifier getModelLocation(PortableBarrelItem object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/portable_barrel.geo.json");
    }

    @Override
    public Identifier getTextureLocation(PortableBarrelItem object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/models/armor/portable_barrel.png");

    }

    @Override
    public Identifier getAnimationFileLocation(PortableBarrelItem animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/portable_barrel.animation.json");
    }
}
