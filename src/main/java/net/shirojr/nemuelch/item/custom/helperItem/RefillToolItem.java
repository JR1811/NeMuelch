package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.mixin.access.InventoryAccessor;

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
            BlockEntity blockEntity = context.getWorld().getBlockEntity(positionClicked);
            BlockState targetBlockState = context.getWorld().getBlockState(positionClicked);
            Block targetBlock = targetBlockState.getBlock();

            if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {

                //using Mixin Accessor to get access to the inventory field of ChestBlockEntity
                DefaultedList<ItemStack> chestBlockEntityInv = ((InventoryAccessor) chestBlockEntity).nemuelch$getInventory();

                //show all chest slots


                for (int i = 0; i < chestBlockEntityInv.size(); i++) {

                    ItemStack entry = chestBlockEntityInv.get(i);

                    if (!entry.isEmpty()) {

                        NeMuelch.LOGGER.info(chestBlockEntityInv.get(i).getNbt().toString());
                    }
                }
            }

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

                NeMuelch.LOGGER.info(i + "# "  + itemStack.getName().getString() + " in container: ");
                if (itemStack.getNbt().contains("StoredEnchantments")) {
                    NeMuelch.LOGGER.info(itemStack.getNbt().get("StoredEnchantments").toString());
                }
                if (itemStack.getNbt().contains("Potion")) {
                    NeMuelch.LOGGER.info(itemStack.getNbt().get("Potion").toString());
                }

            }
        }

        NeMuelch.LOGGER.info("size of list: " + storedItems.size());

        //printAllSavedItems();

        if (!storedItems.isEmpty()) storedItems.clear();
    }

    private void printAllSavedItems() {

        for (int i = 0; i < storedItems.size(); i++) {

            String itemName = storedItems.get(i).getName().getString();
            int itemCount = storedItems.get(i).getCount();
            player.sendMessage(new LiteralText(itemName + " : " + itemCount), false);
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
