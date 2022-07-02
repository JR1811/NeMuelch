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
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    private List<StoredItemInTool> storedItems = new ArrayList<>();
    private PlayerEntity player;


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!context.getWorld().isClient()) {

            player = context.getPlayer();

            BlockPos positionClicked = context.getBlockPos();
            BlockState targetBlockState = context.getWorld().getBlockState(positionClicked);
            Block targetBlock = context.getWorld().getBlockState(positionClicked).getBlock();


            getBlueprintChestContent(targetBlock, targetBlockState, player, context.getWorld(), positionClicked);

        }

        return super.useOnBlock(context);
    }

    /**
     * Blueprint chests are already existing chests that contain items.
     * They are defining the content for the new chests.
     * @param targetBlockState State of the chest block (e.g. SINGLE or DOUBLE)
     * @param player Specifies the player for the notification message
     * @return true if block is chest and contains items
     */
    private void getBlueprintChestContent(Block targetBlock, BlockState targetBlockState, PlayerEntity player, World world, BlockPos pos) {

        //is chest
        if(targetBlock instanceof ChestBlock chestBlock) {

            player.sendMessage(new LiteralText("im an instance of type ChestBlock"), false);


            // running through target container inventory
            for (int i = 0; i < chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).size(); i++ ) {

                player.sendMessage(new LiteralText("im checking " + i + " pos in chest"), false);

                ItemStack itemStack = chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).getStack(i);

                if (!itemStack.isEmpty()) {

                    if (player.getMainHandStack().getName().toString().contains("tool") || player.getMainHandStack().getName().toString().contains("Tool")) {

                        player.sendMessage(new LiteralText(player.getMainHandStack().getTranslationKey()), false);

                        //apply new NBT data to refill tool
                        player.getMainHandStack().setNbt(addItemToToolNbt(itemStack));
                    }

                }



            }


            //player.sendMessage(new LiteralText("" + countOfItemsInInv), false);


            //is empty chest
            if (chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).isEmpty()) {

                player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.is_empty_chest"), false);


                //for testing if different chestBlocks are targeted
                player.sendMessage(new TranslatableText(chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).toString()), false);


            }

            //is Blueprint chest
            else {

                // player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.is_blueprint_chest"), false);


                switch(chestBlock.getDoubleBlockType(targetBlockState)) {
                    case  SINGLE:
                        player.sendMessage(new LiteralText("System - I'm a single chest with content"), false);
                        break;
                    case FIRST:     //is double chest (right)
                    case SECOND:    //is double chest (left)
                        player.sendMessage(new LiteralText("System - I'm a double chest with content"), false);
                        break;
                }

            }
        }

        //is no chest
        else {

            player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.is_no_chest"),false);

            //TODO: (re-)place block on top with chest(-s)

        }
    }

    private NbtCompound addItemToToolNbt(ItemStack stack) {


        NbtCompound itemNbt = stack.getNbt();
        NbtCompound toolNbt = player.getMainHandStack().getNbt();
        String stackName = stack.getTranslationKey();

        //TODO: Problem starts here and doesn't execute the if AND the else anymore

        if (toolNbt.contains("countOf" + stackName)) {


            itemNbt.putInt("countOf" + stackName, toolNbt.getInt("countOf_" + stackName) + 1);
        }


        else {


            itemNbt.putInt("countOf" + stackName, itemNbt.getInt("Count"));       //TODO: in-game listed as 64b --> could be of type byte? (getByte())
        }

        player.sendMessage(new LiteralText("added " + stackName + " to list"), false);

        return itemNbt;
    }

    private void printAllSavedItems(ItemStack stack) {


        //TODO: get NBT item keys

    }


    /**
     *
     * @param block block which should be tested for being a chest
     * @return return if block is a chest block
     */
    private boolean isChestBlock(Block block) {
        return block == Blocks.CHEST ||
                block == Blocks.TRAPPED_CHEST ||
                block == Blocks.BARREL ||
                block == Blocks.SHULKER_BOX ||
                block == Blocks.ENDER_CHEST ||
                block == Blocks.HOPPER;
    }
}
