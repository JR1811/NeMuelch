package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnvelopeItem extends Item {
    public EnvelopeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        //TODO: add screen
        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.envelope.tooltip.shift"));
        }

        else {
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        }
    }
}
