package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import org.jetbrains.annotations.Nullable;

public class CrateBlockEntity extends BlockEntity {
    private final SimpleInventory bottomInventory = new SimpleInventory(6);
    private final SimpleInventory topInventory = new SimpleInventory(6);

    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CRATE, pos, state);
    }

    public SimpleInventory getTopInventory() {
        return topInventory;
    }

    public SimpleInventory getBottomInventory() {
        return bottomInventory;
    }

    public SimpleInventory getInventory(@Nullable Vec3d hitPos) {
        BlockState cachedState = getCachedState();
        CrateBlock.Type type = cachedState.get(CrateBlock.TYPE);
        if (type != CrateBlock.Type.DOUBLE || hitPos == null) {
            return getBottomInventory();
        }
        if (hitPos.getY() <= 0.5) return getTopInventory();
        else return getBottomInventory();
    }

    public void onBroken() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.getPos(), this.getBottomInventory());
        ItemScatterer.spawn(serverWorld, this.getPos(), this.getTopInventory());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, bottomInventory.stacks);
        Inventories.readNbt(nbt, topInventory.stacks);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, bottomInventory.stacks);
        Inventories.writeNbt(nbt, topInventory.stacks);
    }
}
