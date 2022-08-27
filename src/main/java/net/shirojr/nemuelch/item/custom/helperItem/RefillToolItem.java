package net.shirojr.nemuelch.item.custom.helperItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
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
import net.shirojr.nemuelch.NeMuelch;

public class RefillToolItem extends Item {
    public RefillToolItem(Settings settings) {
        super(settings);
    }

    private PlayerEntity player;

    public static final DirectionProperty CHEST_FACING = Properties.HORIZONTAL_FACING;
    public static final DirectionProperty BARREL_FACING = Properties.FACING;

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

                    toolNbt.put("containerContent", nbt);
                    toolNbt.putString("containerType", "chest");

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

            else if (targetBlockEntity instanceof BarrelBlockEntity barrelBlockEntity) {

                // handle full barrel (copy data)
                if (!barrelBlockEntity.isEmpty()) {

                    NbtCompound nbt = barrelBlockEntity.createNbt();
                    NbtCompound toolNbt = context.getStack().getOrCreateNbt();

                    toolNbt.put("containerContent", nbt);
                    toolNbt.putString("containerType", "barrel");

                    player.sendMessage(new TranslatableText("item.nemuelch.entity_transport_tool_entity_registered"), false);
                }

                // handle empty barrel (paste data or error if no data)
                else {

                    if (toolContainsNbt(context.getStack())) {

                        positionClicked = positionClicked.down();
                        createBarrel(context, positionClicked);
                    }

                    else noDataWarning();
                }
            }

            // handle no barrel (create barrel & paste data)
            else if(toolContainsNbt(context.getStack())) {

                if (context.getStack().getNbt().getString("containerType").equals("chest"))
                createChest(context, positionClicked);

                else if (context.getStack().getNbt().getString("containerType").equals("barrel")) {
                    createBarrel(context,positionClicked);
                }
            }

            // handle no barrel & no existing data (error)
            else noDataWarning();
        }

        return super.useOnBlock(context);
    }

    private void createChest(ItemUsageContext context, BlockPos positionClicked) {

        BlockState newChestBlock = Blocks.CHEST.getDefaultState().with(CHEST_FACING, player.getHorizontalFacing().getOpposite());
        context.getWorld().setBlockState(positionClicked.up(), newChestBlock);

        BlockEntity newChestBlockEntity = context.getWorld().getBlockEntity(positionClicked.up());

        // printing items from blueprint list into chest
        if (newChestBlockEntity instanceof ChestBlockEntity chestBlockEntity) {

            chestBlockEntity.readNbt(context.getStack().getSubNbt("containerContent"));
        }
    }

    private void createBarrel(ItemUsageContext context, BlockPos positionClicked) {

        BlockState newBarrelBlock = Blocks.BARREL.getDefaultState().with(BARREL_FACING, player.getHorizontalFacing().getOpposite());
        context.getWorld().setBlockState(positionClicked.up(), newBarrelBlock);

        BlockEntity newBarrelBlockEntity = context.getWorld().getBlockEntity(positionClicked.up());

        // printing items from blueprint list into chest
        if (newBarrelBlockEntity instanceof BarrelBlockEntity barrelBlockEntity) {

            barrelBlockEntity.readNbt(context.getStack().getSubNbt("containerContent"));
        }

    }

    private void noDataWarning() {

        player.sendMessage(new TranslatableText("item.nemuelch.refill_tool.no_blueprint"), false);
    }


    private boolean toolContainsNbt(ItemStack toolStack) {

        if (toolStack.hasNbt()) {

            return !toolStack.getSubNbt("containerContent").isEmpty();
        }

        return false;
    }
}
