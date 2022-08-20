package net.shirojr.nemuelch.item.custom.helperItem;

import com.mojang.brigadier.StringReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.mixin.access.InventoryAccessor;

import java.util.*;
import java.util.stream.Collectors;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    private List<ItemStack> storedItems = new ArrayList<>();
    private List<RefillToolItemEntry> toolEntryList = new ArrayList<>();
    private PlayerEntity player;

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!context.getWorld().isClient()) {

            player = context.getPlayer();
            BlockPos positionClicked = context.getBlockPos();
            BlockEntity targetBlockEntity = context.getWorld().getBlockEntity(positionClicked);
            BlockState targetBlockState = context.getWorld().getBlockState(positionClicked);
            Block targetBlock = targetBlockState.getBlock();

            // scanning targeted block
            if(targetBlockEntity instanceof ChestBlockEntity chestBlockEntity) {

                DefaultedList<ItemStack> chestBlockEntityInv = ((InventoryAccessor) chestBlockEntity).nemuelch$getInventory();
                getChestContent(chestBlockEntityInv);

                NbtCompound nbt = chestBlockEntity.createNbt();
                NbtCompound toolNbt = context.getStack().getOrCreateNbt();

                toolNbt.put("chestContent", nbt);
            }

            // create new chest
            else if(toolEntryList.size() > 0) {

                BlockState newChestBlock = Blocks.CHEST.getDefaultState().with(FACING, player.getHorizontalFacing().getOpposite());
                context.getWorld().setBlockState(positionClicked.up(), newChestBlock);

                BlockEntity newChestBlockEntity = context.getWorld().getBlockEntity(positionClicked.up());

                // printing items from blueprint list into chest
                if (newChestBlockEntity instanceof ChestBlockEntity chestBlockEntity) {

                    chestBlockEntity.readNbt(context.getStack().getSubNbt("chestContent"));

                }
            }

            // no chest targeted and empty blueprint list
            else {

            }
        }

        return super.useOnBlock(context);
    }

    private void getChestContent(DefaultedList<ItemStack> chestBlockEntityInv) {

        toolEntryList.clear();

        for (int i = 0; i < chestBlockEntityInv.size(); i++) {

            if (!chestBlockEntityInv.get(i).isEmpty()) {

                ItemStack entry = chestBlockEntityInv.get(i);
                int stackCount = entry.getCount();

                toolEntryList.add(new RefillToolItemEntry(entry, stackCount));
            }
        }

    }

    private void applyNbtDataToTool(List<RefillToolItemEntry> itemList, ItemStack toolStack) {

        NbtCompound toolStackNbt = new NbtCompound();


        for (int i = 0; i < itemList.size(); i++) {

            //putting information into String content -> count | nbt if available
            String key = itemList.get(i).getItemTranslationKey();
            String count = "" + itemList.get(i).getCount();
            String nbt = "";
            if (itemList.get(i).hasNbtData()) nbt += itemList.get(i).getItemStack().getNbt();   // add nbt string if necessary

            String itemInfo = count + "|" + nbt;  //if String's last char is not '|' it has NBT !
            NeMuelch.LOGGER.info(key +": " + itemInfo);

            //applying string nbt to toolStack
            toolStackNbt.putString(key, itemInfo);

        }
        toolStack.setNbt(toolStackNbt);

    }

    private void debugSavedItemsInList() {

        for (int i = 0; i < toolEntryList.size(); i++) {

            String itemName = toolEntryList.get(i).getItemTranslationKey();
            int itemCount = toolEntryList.get(i).getCount();

            NeMuelch.LOGGER.info(i + "# Entry in List | " + itemName + " | Count: " + itemCount);
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
