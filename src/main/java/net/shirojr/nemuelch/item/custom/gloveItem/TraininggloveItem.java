package net.shirojr.nemuelch.item.custom.gloveItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.item.materials.NeMuelchMaterials;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TraininggloveItem extends SwordItem implements IAnimatable {

    public AnimationFactory factory = new AnimationFactory(this);


    public TraininggloveItem(Settings settings) {
        super(NeMuelchMaterials.INSTANCE, 0, 5, settings);
    }

    //region animation
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.training_gloves.idle", false));

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));

    }

    @Override
    public AnimationFactory getFactory() {

        return this.factory;
    }
    //endregion


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        //regenerating only if enemy has the same item equipped
        if (target instanceof PlayerEntity enemyPlayer) {

            if (enemyPlayer.getMainHandStack().getItem() == stack.getItem() ||
                    enemyPlayer.getOffHandStack().getItem() == stack.getItem()) {

                target.heal(target.getMaxHealth() - target.getHealth());
            }
        }

        target.playSound(SoundEvents.BLOCK_WOOL_PLACE, 1f, 1f);

        return super.postHit(stack, target, attacker);
    }
}
