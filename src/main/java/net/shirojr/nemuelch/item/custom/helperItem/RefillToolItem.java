package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    private PlayerEntity player;

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        if (!context.getWorld().isClient()) {

            player = context.getPlayer();
            BlockPos positionClicked = context.getBlockPos();
            BlockEntity targetBlockEntity = context.getWorld().getBlockEntity(positionClicked);

            // scanning targeted block
            if(targetBlockEntity instanceof ChestBlockEntity chestBlockEntity) {

                // handle full chest (copy data)
                if (!chestBlockEntity.isEmpty()) {

                    NbtCompound nbt = chestBlockEntity.createNbt();
                    NbtCompound toolNbt = context.getStack().getOrCreateNbt();

                    toolNbt.put("chestContent", nbt);

                    player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.items_registered"), false);
                }

                // handle empty chest (paste data or error if no data)
                else {

                    if (toolContainsNbt(context.getStack())) {

                        positionClicked = positionClicked.down();
                        createChest(context, positionClicked);
                    }

                    else noDataWarning();
                }
            }

            // handle no chest (create chest & paste data)
            else if(toolContainsNbt(context.getStack())) {

                createChest(context, positionClicked);
            }

            // handle no chest & no existing data (error)
            else noDataWarning();
        }

        return super.useOnBlock(context);
    }

    private void createChest(ItemUsageContext context, BlockPos positionClicked) {

        BlockState newChestBlock = Blocks.CHEST.getDefaultState().with(FACING, player.getHorizontalFacing().getOpposite());
        context.getWorld().setBlockState(positionClicked.up(), newChestBlock);

        BlockEntity newChestBlockEntity = context.getWorld().getBlockEntity(positionClicked.up());

        // printing items from blueprint list into chest
        if (newChestBlockEntity instanceof ChestBlockEntity chestBlockEntity) {

            chestBlockEntity.readNbt(context.getStack().getSubNbt("chestContent"));
        }
    }

    private void noDataWarning() {

        player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.no_blueprint"), false);
    }


    private boolean toolContainsNbt(ItemStack toolStack) {

        if (toolStack.hasNbt()) {

            return !toolStack.getSubNbt("chestContent").isEmpty();
        }

        return false;
    }
}
