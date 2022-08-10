package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;

import java.util.ArrayList;
import java.util.List;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    private List<ItemStack> storedItems = new ArrayList<>();
    private NbtCompound savedChestNBT = new NbtCompound();
    private PlayerEntity player;

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!context.getWorld().isClient()) {

            player = context.getPlayer();
            BlockPos positionClicked = context.getBlockPos();
            BlockState targetBlockState = context.getWorld().getBlockState(positionClicked);
            Block targetBlock = targetBlockState.getBlock();

            if(targetBlock instanceof ChestBlock chestBlock) {

                handleChestContent(targetBlockState, chestBlock, context.getWorld(), positionClicked, player.getMainHandStack());
            }
        }

        return super.useOnBlock(context);
    }

    private void handleChestContent(BlockState targetBlockState, ChestBlock chestBlock, World world, BlockPos pos, ItemStack toolStack) {

        // running through target container inventory
        for (int i = 0; i < chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).size(); i++ ) {



            ItemStack itemStack = chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).getStack(i);

            if (!itemStack.isEmpty()) {
                storedItems.add(itemStack);
                player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.item_registered"), false);
            }
        }

        NeMuelch.LOGGER.info("initiating NBT applying process with " + storedItems.size() + " elements");
        NeMuelch.LOGGER.info("first item NBT data: " + storedItems.get(0).getOrCreateNbt());

        // put saved items into tool nbt data
        for (int i = 0; i < storedItems.size();i++) {

            NbtCompound toolNbt = new NbtCompound();

            toolNbt.put("item_" + i, storedItems.get(i).getNbt());
            NeMuelch.LOGGER.info(storedItems.get(i).getNbt() + "");
            toolStack.setNbt(toolNbt);
            toolStack.getOrCreateNbt().put("item_" + i, storedItems.get(i).getNbt());
            toolStack.writeNbt(toolNbt);

        }

        printAllSavedItems();

        if (!storedItems.isEmpty()) storedItems.clear();
    }

    private void printAllSavedItems() {

        for (int i = 0; i < storedItems.size(); i++) {

            String itemName = storedItems.get(i).getName().getString();
            int itemCount = storedItems.get(i).getCount();
            player.sendMessage(new LiteralText(itemName + ": " + itemCount), false);
        }
    }

    private boolean isBlueprintContainer(BlockState targetBlockState, World world, BlockPos pos) {

        if(targetBlockState.getBlock() instanceof ChestBlock chestBlock) {

            for (int i = 0; i < chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).size(); i++ ) {

                ItemStack itemStack = ChestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).getStack(i);
                if (!itemStack.isEmpty()) return true;
            }
        }
        return false;
    }
}
