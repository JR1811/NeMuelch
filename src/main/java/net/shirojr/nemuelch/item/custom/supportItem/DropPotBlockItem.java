package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.DropPotBlockEntity;
import net.shirojr.nemuelch.item.NeMuelchItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DropPotBlockItem extends BlockItem {
    public DropPotBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static void applyInventory(ItemStack stack, DefaultedList<ItemStack> inventory) {
        Inventories.writeNbt(stack.getOrCreateNbt(), inventory);
    }

    public static ItemStack withInventory(DefaultedList<ItemStack> inventory) {
        ItemStack stack = NeMuelchItems.DROP_POT_BLOCK.getDefaultStack();
        applyInventory(stack, inventory);
        return stack;
    }

    @Nullable
    public static DefaultedList<ItemStack> getInventory(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("Items")) return null;
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(DropPotBlockEntity.SLOT_SIZE, ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory);
        return inventory;
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (!(world.getBlockEntity(pos) instanceof DropPotBlockEntity blockEntity)) {
            return super.postPlacement(pos, world, player, stack, state);
        }
        DefaultedList<ItemStack> stackInventory = getInventory(stack);
        if (stackInventory != null) {
            blockEntity.replaceContent(stackInventory);
        }
        return super.postPlacement(pos, world, player, stack, state);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        DefaultedList<ItemStack> inventory = getInventory(stack);
        if (inventory != null) {
            for (ItemStack storedStack : inventory) {
                if (storedStack.isEmpty()) continue;
                Text stackInformation = new LiteralText("%s x ".formatted(storedStack.getCount())).append(storedStack.getName());
                tooltip.add(stackInformation);
            }
        }
    }
}
