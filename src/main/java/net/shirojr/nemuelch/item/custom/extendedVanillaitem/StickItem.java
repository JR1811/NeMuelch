package net.shirojr.nemuelch.item.custom.extendedVanillaitem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.init.ConfigInit;
import net.shirojr.nemuelch.util.NeMuelchTags;

import static net.minecraft.block.CampfireBlock.LIT;

//this vanilla item gets registered from a redirect mixin (ItemsMixin) to apply this custom class

public class StickItem extends Item {
    public StickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());

        if (state.getBlock() != Blocks.CAMPFIRE || !state.get(LIT)) return ActionResult.PASS;
        if (!Registry.BLOCK.getOrCreateEntry(Registry.BLOCK.getKey(state.getBlock())
                .get()).isIn(NeMuelchTags.Blocks.TORCH_IGNITING_BLOCKS)) return ActionResult.PASS;

        if (ConfigInit.CONFIG.campfireUtilities) {
            context.getWorld().playSound(context.getPlayer(), context.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1f, 1f);
            context.getStack().decrement(1);
            context.getPlayer().giveItemStack(new ItemStack(Items.TORCH).copy());

            return ActionResult.success(context.getWorld().isClient());
        }

        return super.useOnBlock(context);
    }
}
