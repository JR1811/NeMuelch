package net.shirojr.nemuelch.item.custom.extendedVanillaitem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;

import static net.minecraft.block.CampfireBlock.LIT;

public class StickItem extends Item {
    public StickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(context.getBlockPos());

        if (player == null) return ActionResult.PASS;
        if (state.getBlock() != Blocks.CAMPFIRE || !state.get(LIT)) return ActionResult.PASS;
        if (!Registry.BLOCK.getOrCreateEntry(Registry.BLOCK.getKey(state.getBlock())
                .get()).isIn(NeMuelchTags.Blocks.TORCH_IGNITING_BLOCKS)) return ActionResult.PASS;

        if (NeMuelchConfigInit.CONFIG.campfireUtilities) {
            if (!world.isClient()) {
                world.playSound(null, context.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                        SoundCategory.BLOCKS, 1f, 1f);
                context.getStack().decrement(1);
                player.giveItemStack(new ItemStack(Items.TORCH).copy());
            }

            return ActionResult.success(context.getWorld().isClient());
        }

        return super.useOnBlock(context);
    }
}
