package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class BandageItem extends Item {
    public BandageItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 200), user);
                if (entity.hasStatusEffect(StatusEffects.NAUSEA)) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200), user);
                }
                entity.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 0.5f, 0.75f);
                stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
