package net.shirojr.nemuelch.entity.Client;

import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.JuggernautEntity;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class JuggernautModel extends AnimatedGeoModel<JuggernautEntity> {
    @Override
    public Identifier getModelLocation(JuggernautEntity object) {
        return new Identifier(NeMuelch.MOD_ID, "geo/juggernaut.geo.json");
    }

    @Override
    public Identifier getTextureLocation(JuggernautEntity object) {
        return new Identifier(NeMuelch.MOD_ID, "textures/entity/juggernaut/juggernaut.png");
    }

    @Override
    public Identifier getAnimationFileLocation(JuggernautEntity animatable) {
        return new Identifier(NeMuelch.MOD_ID, "animations/juggernaut.animations.json");
    }


    @Override
    public void setLivingAnimations(JuggernautEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");

        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 300F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 300F));
        }
    }
}
