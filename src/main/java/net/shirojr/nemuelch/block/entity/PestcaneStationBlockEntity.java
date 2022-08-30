package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.PestcaneStationBlock;
import net.shirojr.nemuelch.item.NeMuelchItems;
import net.shirojr.nemuelch.recipe.PestcaneStationRecipe;
import net.shirojr.nemuelch.screen.PestcaneStationScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PestcaneStationBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    private final PropertyDelegate propertyDelegate;
    private int progress = 0;
    private int maxProgress = 172;   // sets progress time
    //private int fuelTime = 0;
    //private int maxFuelTime = 0;

    public PestcaneStationBlockEntity(BlockPos pos, BlockState state) {

        super(NeMuelchBlockEntities.PESTCANE_STATION, pos, state);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return PestcaneStationBlockEntity.this.progress;
                    case 1: return PestcaneStationBlockEntity.this.maxProgress;
                    default: return 0;
                }
            }

            public void set(int index, int value) {
                switch(index) {
                    case 0: PestcaneStationBlockEntity.this.progress = value; break;
                    case 1: PestcaneStationBlockEntity.this.maxProgress = value; break;
                }
            }

            public int size() {
                return 2;
            }
        };
    }


    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.nemuelch.pestcane_station_gui_title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new PestcaneStationScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("pestcane_station.progress", progress);
    }

    @Override
    public void readNbt(NbtCompound nbt) {

        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
        progress = nbt.getInt("pestcane_station.progress");
    }

    public static void tick(World world, BlockPos blockPos, BlockState blockState, PestcaneStationBlockEntity entity) {

        if (!world.isClient) {

            BlockState state = world.getBlockState(blockPos.down());
            boolean blockBelowHasHeat = state.getBlock() instanceof AbstractFurnaceBlock && state.get(AbstractFurnaceBlock.LIT);

            if (hasRecipe(entity) && blockBelowHasHeat) {

                entity.progress++;
                blockState = blockState.with(PestcaneStationBlock.LIT, true);
                markDirty(world, blockPos, blockState);

                if (entity.progress >= entity.maxProgress) {

                    blockState = blockState.with(PestcaneStationBlock.LIT, false);
                    markDirty(world, blockPos, blockState);

                    craftItem(entity);
                }

            }

            else {

                entity.resetProgress();
                blockState = blockState.with(PestcaneStationBlock.LIT, false);
                markDirty(world, blockPos, blockState);
            }

            world.setBlockState(blockPos, blockState, Block.NOTIFY_ALL);
        }
    }

    private void resetProgress() {

        this.progress = 0;
    }

    private static void craftItem(PestcaneStationBlockEntity entity) {

        SimpleInventory inventory = new SimpleInventory(entity.size());

        for (int i = 0; i < entity.size(); i++) {

            inventory.setStack(i, entity.getStack(i));
        }

        Optional<PestcaneStationRecipe> recipe = entity.getWorld().getRecipeManager()
                .getFirstMatch(PestcaneStationRecipe.Type.INSTANCE, inventory, entity.getWorld());

        if (hasRecipe(entity)) {
            entity.removeStack(0, 1);
            entity.removeStack(1, 1);

            entity.setStack(2, new ItemStack(recipe.get().getOutput().getItem(),
                    recipe.get().getOutput().getCount()));

            entity.resetProgress();
        }
    }

    private static boolean hasRecipe(PestcaneStationBlockEntity entity) {

        SimpleInventory inventory = new SimpleInventory(entity.size());

        for (int i = 0; i < entity.size(); i++) {

            inventory.setStack(i, entity.getStack(i));
        }

        Optional<PestcaneStationRecipe> match = entity.getWorld().getRecipeManager()
                .getFirstMatch(PestcaneStationRecipe.Type.INSTANCE, inventory, entity.getWorld());

        return match.isPresent() && canInsertAmountIntoOutputSlot(inventory)
                && canInsertItemIntoOutputSlot(inventory, match.get().getOutput().getItem());
    }

    private boolean isBurning() {
        return this.progress > 0;
    }

    private static boolean canInsertItemIntoOutputSlot(SimpleInventory inventory, Item output) {

        return inventory.getStack(2).getItem() == output || inventory.getStack(2).isEmpty();
    }

    private static boolean canInsertAmountIntoOutputSlot(SimpleInventory inventory) {

        return inventory.getStack(2).getMaxCount() > inventory.getStack(2).getCount();
    }
}
