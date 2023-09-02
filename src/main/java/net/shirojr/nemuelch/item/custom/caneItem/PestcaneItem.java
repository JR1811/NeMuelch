package net.shirojr.nemuelch.item.custom.caneItem;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PestcaneItem extends Item implements IAnimatable {

    //protected static final UUID ATTACK_KNOCKBACK_MODIFIER_ID = UUID.randomUUID();

    public AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public PestcaneItem(Settings settings) {
        super(settings);
    }

    //region animation stuff...
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.pestcane.handleslip", ILoopType.EDefaultLoopTypes.PLAY_ONCE));

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
            if (entity instanceof PlayerEntity player) {

                if (player.getMainHandStack() == stack || player.getOffHandStack() == stack) {
                    applyEffect(player);
                }
            }
        }
    }

    private void applyEffect(PlayerEntity player) {
        boolean hasSlownessEffect = player.hasStatusEffect(StatusEffects.SLOWNESS);

        if (!hasSlownessEffect) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                    100, 0, true, false));
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

    /**
     * Thanks to 🕊 Aquaglyph 🕊#7209 on the fabric discord for helping out with the KnockBack mixin <br>
     * check {@link net.shirojr.nemuelch.mixin.PlayerEntityMixin#nemuelch$applyDefaultKnockbackFromStack(int) PlayerEntityMixin}
     */
    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {

        String name = "nemuelch_pestcane_knockback";
        double knockBackValue = 2.0;

        Multiset<EntityAttribute> keys = super.getAttributeModifiers(slot).keys();
        Multimap<EntityAttribute, EntityAttributeModifier> newAtributeModifiers = ArrayListMultimap.create();

        if (keys != null) {
            keys.forEach(entityAttribute -> {
                super.getAttributeModifiers(slot).get(entityAttribute).forEach(entityAttributeModifier -> {
                    newAtributeModifiers.put(entityAttribute, entityAttributeModifier);
                });
            });
        }

        newAtributeModifiers.put(
                EntityAttributes.GENERIC_ATTACK_KNOCKBACK,
                new EntityAttributeModifier(
                        name, knockBackValue, EntityAttributeModifier.Operation.ADDITION));


        return newAtributeModifiers;
    }
    //endregion
}
