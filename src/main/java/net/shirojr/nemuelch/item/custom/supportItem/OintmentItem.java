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

public class OintmentItem extends Item {
    public OintmentItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), user);
                entity.playSound(SoundEvents.ITEM_HONEY_BOTTLE_DRINK, 0.5f, 0.75f);
                stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
