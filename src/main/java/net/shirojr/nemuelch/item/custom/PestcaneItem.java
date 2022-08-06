package net.shirojr.nemuelch.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

public class PestcaneItem extends Item implements IAnimatable {

    protected static final UUID ATTACK_KNOCKBACK_MODIFIER_ID = UUID.randomUUID();

    public AnimationFactory factory = new AnimationFactory(this);

    public PestcaneItem(Settings settings) {
        super(settings);
    }

    //region animation stuff...
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
    //endregion

    //region effects on user
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player);

                    NeMuelch.LOGGER.info("Der PestCane Stack: " + stack);

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
    //endregion

    //region effects on target
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 1, false, false, false), attacker);

        return super.postHit(stack, target, attacker);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {

        int knockbackValue = 30;    // TODO: change this test value
        String name = "Base Item Knockback modifier";

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(ATTACK_KNOCKBACK_MODIFIER_ID,
                name, knockbackValue, EntityAttributeModifier.Operation.ADDITION));

        //TODO: write mixin for PlayerEntity in the attack() method
        return builder.build();
    }
    //endregion
}
