package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
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


            //TODO: could be shorter if rearranged
            if (!isChestBlock(targetBlock)) {
                player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.no_chest"), false);
            }

            else {
                if(isBlueprintChest(targetBlockState(), targetBlockState, player, context.getWorld(), positionClicked, true, isChestBlock(targetBlock))) {
                    player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.is_blueprint_chest"), false);

                }

                else {
                    player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.is_empty_chest"), false);
                }
            }
        }

        return super.useOnBlock(context);
    }

    /**
     * Blueprint chests are allready existing chests that contain items.
     * They are defining the content for the new chests.
     * @param state State of the chest block (e.g. SINGLE or DOUBLE)
     * @param player Specifies the player for the notification message
     * @param isChest Checks if targeted block is a chest block
     * @return true if block is chest and contains items
     */
    private boolean isBlueprintChest(ChestBlock block, BlockState state, PlayerEntity player, World world, BlockPos pos, boolean ignoreBlocked, boolean isChest) {

        //Blueprint Chest
        if (isChest && ChestBlock.getInventory(block, state, world, pos, true) == null) {  //TODO: also check if chest has content --> does it work?

            //check for chest size
            switch(ChestBlock.getDoubleBlockType(state)) {
                case  SINGLE:
                    player.sendMessage(new LiteralText("System - I'm a single chest"), false);
                    //TODO: save in an extra field or variable. Check how chests remember their content
                    break;
                case FIRST:     //is double chest (right)
                case SECOND:    //is double chest (left)
                    player.sendMessage(new LiteralText("System - I'm a double chest"), false);
                    //TODO: save in an extra field or variable. Check how Shulker remember their content
                    break;
            }
            return true;
        }

        //new Chest
        else {

            player.sendMessage(new LiteralText("item.nemuelch.refill_tool.is_no_chest"),false);

            return  false;
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

    //TODO: remember content items and content items count
    //TODO: List amount of item appearance in blueprint chests as chat msg
    //TODO: (re-)place block on top with chest(-s)


    //useful functions

    /*
    public Item.Settings maxCount();

    public ActionResult useOnBlock();

    public boolean onStackClicked();
    public boolean onClicked();

    public ActionResult useOnEntity();


     */
}
