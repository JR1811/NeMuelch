package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.ointment.tooltip.shift"));
        }

        else {
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
