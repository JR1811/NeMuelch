package net.shirojr.nemuelch.item.custom.castAndMagicItem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArtifactItem extends Item {
    public ArtifactItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (!Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.tooltip.desc1"));
            tooltip.add(new TranslatableText("item.nemuelch.tooltip.expand.line2"));
        } else {
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.unknown1"));
            tooltip.add(new TranslatableText("item.nemuelch.blocked_book_artifact.unknown2"));
        }
    }
}
