package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.component.RottenMeatDigestionComponent;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;

/**
 * Main digestion logic is handled in {@link RottenMeatDigestionComponent RottenMeatDigestionComponent} using CCA
 */
public class RottenMeatBlockEntity extends BlockEntity {
    private ItemStack toBeConsumed;

    public RottenMeatBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, ItemStack.EMPTY);
    }

    public RottenMeatBlockEntity(BlockPos pos, BlockState state, ItemStack toBeConsumed) {
        super(NeMuelchBlockEntities.ROTTEN_MEAT, pos, state);
        this.toBeConsumed = toBeConsumed;
    }

    public ItemStack getJumpStartStack() {
        return toBeConsumed;
    }

    public ItemStack clearJumpStartStack() {
        ItemStack removedStack = this.toBeConsumed.copy();
        this.toBeConsumed = ItemStack.EMPTY;
        markDirty();
        return removedStack;
    }

    @SuppressWarnings("unused")
    public RottenMeatDigestionComponent getDigestion() {
        return RottenMeatDigestionComponent.get(this);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.toBeConsumed = ItemStack.fromNbt(nbt.getCompound("JumpStart"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtCompound jumpStartNbt = new NbtCompound();
        toBeConsumed.writeNbt(jumpStartNbt);
        nbt.put("JumpStart", jumpStartNbt);
    }
}
