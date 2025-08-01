package net.shirojr.nemuelch.item.custom;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MuelchItem extends HoneyBottleItem {
    private final List<StatusEffectInstance> effects;
    private final int tooltipLines;

    public MuelchItem(Settings settings, List<StatusEffectInstance> effects, int tooltipLines) {
        super(settings);
        this.effects = effects;
        this.tooltipLines = tooltipLines;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        if (user instanceof PlayerEntity && !((PlayerEntity) user).getAbilities().creativeMode) {
            stack.decrement(1);
        }
        if (!world.isClient()) {
            user.clearStatusEffects();
            for (StatusEffectInstance entry : this.effects) {
                user.addStatusEffect(entry);
            }
        }
        return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (Screen.hasShiftDown()) {
            String itemName = Registries.ITEM.getId(this).getPath();
            for (int i = 1; i < this.tooltipLines; i++) {
                tooltip.add(Text.translatable("item.nemuelch.%s.tooltip.shift.line%s".formatted(itemName, i)));
            }
        } else {
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(Text.translatable("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
