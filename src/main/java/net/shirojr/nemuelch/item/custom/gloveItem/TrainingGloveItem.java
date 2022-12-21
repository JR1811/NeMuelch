package net.shirojr.nemuelch.item.custom.gloveItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.item.materials.GloveMaterial;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;

public class TrainingGloveItem extends SwordItem implements IAnimatable {

    public AnimationFactory factory = new AnimationFactory(this);
    public static final String NBT_KEY_GLOVE_HIT = "glove_hit";

    public TrainingGloveItem(Settings settings) {
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

            // assign glove stack
            ItemStack targetGloveStack;
            if (enemyPlayer.getMainHandStack().getItem() == stack.getItem()) {
                targetGloveStack = enemyPlayer.getMainHandStack();
            }
            else if (enemyPlayer.getOffHandStack().getItem() == stack.getItem()) {
                targetGloveStack = enemyPlayer.getOffHandStack();
            }

            else {
                return super.postHit(stack, target, attacker);
            }

            // regenerating only if enemy has the same item equipped
            NbtCompound enemyGloveNbt = targetGloveStack.getOrCreateNbt();

            if (!enemyGloveNbt.contains(NBT_KEY_GLOVE_HIT)) {
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, 1);
            }
            else {
                int oldHitValue = enemyGloveNbt.getInt(NBT_KEY_GLOVE_HIT);
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, oldHitValue + 1);
            }

            // points reached critical amount
            if (enemyGloveNbt.getInt(NBT_KEY_GLOVE_HIT) > ConfigInit.CONFIG.trainingGloveMaxHits) {

                target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,
                        100, 0, true, false));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS,
                        40, 1, true, false));

                target.playSound(SoundEvents.BLOCK_BELL_RESONATE, 2f, 1f);
                targetGloveStack.getOrCreateNbt().putInt(NBT_KEY_GLOVE_HIT, 0);
            }

            target.heal(target.getMaxHealth() - target.getHealth());
        }

        target.playSound(SoundEvents.BLOCK_WOOL_PLACE, 1f, 1f);

        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        TranslatableText description = new TranslatableText("item.nemuelch.training_glove.description");
        LiteralText counter = new LiteralText("§e" + stack.getOrCreateNbt().getInt(NBT_KEY_GLOVE_HIT) + "§r");
        tooltip.add(description.append(counter));
    }
}
