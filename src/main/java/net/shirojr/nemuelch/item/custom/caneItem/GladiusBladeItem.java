package net.shirojr.nemuelch.item.custom.caneItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.item.NeMuelchItems;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class GladiusBladeItem extends SwordItem implements IAnimatable {

    private static final int USE_COOLDOWN_TICKS = 80;

    public AnimationFactory factory = new AnimationFactory(this);

    // ctor
    public GladiusBladeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    //region animation
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.gladiuscane.stickshift", false));

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

    //region effects on target
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        applyEffect(target, 40, 0, StatusEffects.GLOWING);

        if (!target.isAlive()) {

            applyEffect(attacker, 200, 2, StatusEffects.STRENGTH);
            applyEffect(attacker, 100, 3, StatusEffects.SPEED);
        }

        /*
        // functionality for another cane?
        else {

            target.setVelocity(0, 1.5, 0);
            target.velocityModified = true;
            target.fallDistance = 0.0f;
        }
         */

        return super.postHit(stack, target, attacker);
    }
    //endregion

    //region effects on user
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity player) {

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player, 100, 1, StatusEffects.SLOWNESS);
                }
            }
        }
    }

    private void applyEffect (Entity entity, int duration, int amplifier, StatusEffect statusEffect) {

        if (entity instanceof LivingEntity target) {

            if (!target.hasStatusEffect(statusEffect)) {

                target.addStatusEffect(new StatusEffectInstance(statusEffect,
                        duration, amplifier, true, false));
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1f, 1f);

        ItemStack itemStack = new ItemStack(NeMuelchItems.GLADIUS_CANE).copy();
        user.getItemCooldownManager().set(itemStack.getItem(), USE_COOLDOWN_TICKS);

        user.setStackInHand(hand, itemStack);

        return TypedActionResult.success(itemStack, world.isClient());
    }
    //endregion


}
