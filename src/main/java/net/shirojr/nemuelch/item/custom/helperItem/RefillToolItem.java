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

            ItemStack stack = context.getStack();
            NbtCompound nbt;



            //test for tool NBT data
            /*if (stack.hasNbt()) {
                nbt = stack.getNbt();
            }
            else {
                nbt = new NbtCompound();
            }

            if (nbt.contains("testString")) {
                nbt.putString("testString", "schon etwas gespeichert!");
            }

            else {
                nbt.putString("testString", "der testString fuer Nbt daten");
            }
            context.getStack().setNbt(nbt);*/


            if(targetBlock instanceof ChestBlock chestBlock) {

                handleChestContent(targetBlockState, chestBlock, context.getWorld(), positionClicked, stack);
            }


        }

        return super.useOnBlock(context);
    }

    /**
     * Blueprint chests are already existing chests that contain items.
     * @param targetBlockState
     * @param chestBlock
     * @param world world in which the container exists
     * @param pos clicked position
     * @param toolStack ItemStack of tool which will get the nbt data
     */
    private void handleChestContent(BlockState targetBlockState, ChestBlock chestBlock, World world, BlockPos pos, ItemStack toolStack) {

        // running through target container inventory
        for (int i = 0; i < chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).size(); i++ ) {

            ItemStack itemStack = chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).getStack(i);

            if (!itemStack.isEmpty()) {
                player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.item_registered"), false);
                storedItems.add(itemStack);
            }
        }

        // put saved items into tool nbt data
        for (int i = 0; i < storedItems.size();i++) {

            NbtCompound toolNbt = new NbtCompound();
            NbtList toolNbtList = new NbtList();

            toolNbt.put("item_" + i, storedItems.get(i).getNbt());
            toolStack.setNbt(toolNbt);
        }

        printAllSavedItems();

        if (storedItems.size() > 0) storedItems.clear();
    }

    private void printAllSavedItems() {

        for (int i = 0; i < storedItems.size(); i++) {

            String itemName = storedItems.get(i).getName().getString();
            int itemCount = storedItems.get(i).getCount();
            player.sendMessage(new LiteralText(itemName + ": " + itemCount), false);

        }
    }

    private boolean isContainerBlock(Block block) {
        return block == Blocks.CHEST ||
                block == Blocks.TRAPPED_CHEST ||
                block == Blocks.BARREL ||
                block == Blocks.SHULKER_BOX;
    }
}
