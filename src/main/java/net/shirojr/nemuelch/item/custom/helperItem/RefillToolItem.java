package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    //TODO: catch if block is chest but empty


    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (context.getWorld().isClient()) {
            BlockPos positionClicked = context.getBlockPos();
            PlayerEntity player = context.getPlayer();
            BlockState targetBlockState = context.getWorld().getBlockState(positionClicked);
            Block targetBlock = context.getWorld().getBlockState(positionClicked).getBlock();



            blueprintChest(targetBlock, targetBlockState, player, context.getWorld(), positionClicked);

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
    private void blueprintChest(Block targetBlock, BlockState targetBlockState, PlayerEntity player, World world, BlockPos pos) {

        //is chest
        if(targetBlock instanceof ChestBlock chestBlock) {

            int countOfItemsInInv = 0;


            //  test for counting items in inv
            for (int i = 0; i < chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).size(); i++ ) {

                ItemStack stack = chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).getStack(i);

                if (stack.isEmpty()) continue;

                Item item = stack.getItem();
                player.sendMessage(new LiteralText("" + item.getName() + ""), false);
                countOfItemsInInv++;
            }

            player.sendMessage(new LiteralText("" + countOfItemsInInv), false);






            //is empty chest
            if (chestBlock.getInventory(chestBlock, targetBlockState, world, pos, true).isEmpty()) {    //TODO: doesn't get content in any case?

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



    /**
     *
     * @param block block which should be tested for being a chest
     * @return return if block is a chest block
     */
    private boolean isChestBlock(Block block) {
        return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST;
    }
}
