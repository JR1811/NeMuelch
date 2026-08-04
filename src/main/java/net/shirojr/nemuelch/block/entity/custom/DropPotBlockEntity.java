package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.inventory.HandledInventory;
import net.shirojr.nemuelch.item.custom.supportItem.DropPotBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DropPotBlockEntity extends BlockEntity implements HandledInventory {
    public static final int SLOT_SIZE = 9;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(SLOT_SIZE, ItemStack.EMPTY);
    private boolean shouldDropContent = true;

    public DropPotBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.DROP_BLOCK, pos, state);
    }

    public boolean shouldDropContent() {
        return shouldDropContent;
    }

    public void setShouldDropContent(boolean shouldDropContent) {
        this.shouldDropContent = shouldDropContent;
    }

    public ItemStack asItemStack() {
        return DropPotBlockItem.withInventory(this.inventory);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (stack.getItem() instanceof DropPotBlockItem) {
            if (!DropPotBlockItem.hasEmptyInventory(stack)) return false;
        }
        return HandledInventory.super.canInsert(slot, stack, dir);
    }

    public void dropRandomItem() {
        if (!(this.world instanceof ServerWorld serverWorld)) return;
        List<Pair<Integer, ItemStack>> validStacks = new ArrayList<>();
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack entryStack = this.inventory.get(i);
            if (!entryStack.isEmpty()) validStacks.add(new Pair<>(i, entryStack));
        }
        if (validStacks.isEmpty()) return;
        int randomIndex = serverWorld.getRandom().nextInt(validStacks.size());
        int stackInInventoryIndex = validStacks.get(randomIndex).getLeft();
        ItemStack stack = this.inventory.get(stackInInventoryIndex).copy();
        ItemScatterer.spawn(serverWorld, this.getPos().getX(), this.getPos().up().getY(), this.getPos().getZ(), stack);
        this.inventory.set(stackInInventoryIndex, ItemStack.EMPTY.copy());
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 2f, 1f);
        serverWorld.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_BRICKS_PLACE, SoundCategory.BLOCKS, 1f, 1f);
        serverWorld.spawnParticles(ParticleTypes.POOF, this.getPos().getX(), this.getPos().up().getY(), this.getPos().getZ(), 0, 0, 0.3, 0, 0.2);
    }

    public void dropInventoryAndClear() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, pos, getItems());
        this.clear();
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, DropPotBlockEntity blockEntity) {

    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, getItems());
        if (nbt.contains("dropContent")) {
            setShouldDropContent(nbt.getBoolean("dropContent"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, getItems());
        nbt.putBoolean("dropContent", shouldDropContent());
    }
}
