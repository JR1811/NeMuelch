package net.shirojr.nemuelch.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
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


    //animations
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("handleslip", false));


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


    //effects on user
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player);

                    //TODO: stack.addEnchantment();
                }
            }
        }
    }

    private void applyEffect (PlayerEntity player) {
        boolean hasSlownessEffect = player.hasStatusEffect(StatusEffects.SLOWNESS);

        if (!hasSlownessEffect) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1, true, false));   //20 tick = 1 sek
        }
    }
}
