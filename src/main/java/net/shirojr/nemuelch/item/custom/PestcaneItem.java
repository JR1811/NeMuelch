package net.shirojr.nemuelch.item.custom;

import net.minecraft.item.Item;
import net.shirojr.nemuelch.NeMuelch;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class PestcaneItem extends Item implements IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);

    public PestcaneItem(Settings settings) {
        super(settings);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("handleslip", false));

        NeMuelch.LOGGER.info("execute PlayState animation in PestcaneItem");

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));

        NeMuelch.LOGGER.info("execute PlayState in PestcaneItem");
    }

    @Override
    public AnimationFactory getFactory() {

        NeMuelch.LOGGER.info("execute getFactory in PestcaneItem");

        return this.factory;
    }
}
