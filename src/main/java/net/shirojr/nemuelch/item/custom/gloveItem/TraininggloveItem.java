package net.shirojr.nemuelch.item.custom.gloveItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.materials.GloveMaterial;
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
        super(GloveMaterial.INSTANCE, 0, ConfigInit.CONFIG.trainingGloveAttackSpeed, settings);
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

        if (target instanceof PlayerEntity enemyPlayer) {

            // regenerating only if enemy has the same item equipped
            if (enemyPlayer.getMainHandStack().getItem() == stack.getItem()) {

                ItemStack enemyGloveStack = enemyPlayer.getMainHandStack();
                NbtCompound enemyGloveNbt = enemyGloveStack.getOrCreateNbt();

                if (!enemyGloveStack.getOrCreateNbt().contains("glove_hit")) {
                    enemyGloveNbt.putInt("glove_hit", 1);
                }
                else {
                    int oldHitValue = enemyGloveStack.getOrCreateNbt().getInt("glove_hit");
                    enemyGloveNbt.putInt("glove_hit", oldHitValue + 1);
                }

                // points reached critical amount
                if (enemyGloveStack.getOrCreateNbt().getInt("glove_hit") > ConfigInit.CONFIG.trainingGloveMaxHits) {

                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,
                            80, 1, true, false));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,
                            40, 1, true, false));

                    target.playSound(SoundEvents.BLOCK_BELL_RESONATE, 2f, 1f);
                    enemyGloveNbt.putInt("glove_hit", 0);
                }

                target.heal(target.getMaxHealth() - target.getHealth());
            }

            if (enemyPlayer.getOffHandStack().getItem() == stack.getItem()) {

                ItemStack enemyGloveStack = enemyPlayer.getOffHandStack();
                NbtCompound enemyGloveNbt = enemyGloveStack.getOrCreateNbt();

                if (!enemyGloveStack.getOrCreateNbt().contains("glove_hit")) {
                    enemyGloveNbt.putInt("glove_hit", 1);
                }
                else {
                    int oldHitValue = enemyGloveStack.getOrCreateNbt().getInt("glove_hit");
                    enemyGloveNbt.putInt("glove_hit", oldHitValue + 1);
                }

                // points reached critical amount
                if (enemyGloveStack.getOrCreateNbt().getInt("glove_hit") > ConfigInit.CONFIG.trainingGloveMaxHits) {

                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,
                            80, 1, true, false));
                    target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,
                            40, 1, true, false));

                    target.playSound(SoundEvents.BLOCK_BELL_RESONATE, 2f, 1f);
                    enemyGloveNbt.putInt("glove_hit", 0);
                }

                target.heal(target.getMaxHealth() - target.getHealth());
            }
        }

        target.playSound(SoundEvents.BLOCK_WOOL_PLACE, 1f, 1f);

        return super.postHit(stack, target, attacker);
    }
}
