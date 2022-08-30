package net.shirojr.nemuelch.item.custom.caneItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

public class ArkaduscaneItem extends Item implements IAnimatable {

    public AnimationFactory factory = new AnimationFactory(this);

    public ArkaduscaneItem(Settings settings) {
        super(settings);
    }

    //region animation stuff...
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.arkaduscane.stickshift", false));

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

    //region effects on user
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player);
                }
            }
        }
    }

    private void applyEffect (PlayerEntity player) {
        boolean hasSlownessEffect = player.hasStatusEffect(StatusEffects.SLOWNESS);

        if (!hasSlownessEffect) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, true, false));   //20 tick = 1 sek
        }
    }
    //endregion

    //region effects on target
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA, 100, 1, false, false, false), attacker);

        return super.postHit(stack, target, attacker);
    }
    //endregion
}
