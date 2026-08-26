package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CargoCrateBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private static final int STACK_PER_BLOCK_COUNT = 27;
    public static final int ORIGINAL_BLOCKS_AMOUNT = 27;
    public static final int INVENTORY_STACKS_AMOUNT = (int) (STACK_PER_BLOCK_COUNT * ORIGINAL_BLOCKS_AMOUNT * 1.5);

    private final DefaultedList<ItemStack> originalBlocks;
    private final CargoCrateInventory inventory;
    private final PropertyDelegate propertyDelegate;

    public CargoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CARGO_CRATE, pos, state);
        this.originalBlocks = DefaultedList.ofSize(ORIGINAL_BLOCKS_AMOUNT, ItemStack.EMPTY);
        this.inventory = new CargoCrateInventory(INVENTORY_STACKS_AMOUNT, this::markDirty);

        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 ->
                            CargoCrateBlockEntity.this.getInventory().size() - CargoCrateBlockEntity.this.getInventory().emptyStacks();
                    case 1 -> CargoCrateBlockEntity.this.getInventory().size();
                    case 2 -> CargoCrateBlockEntity.this.canExtract(1) ? 1 : 0;
                    case 3 -> CargoCrateBlockEntity.this.canExtract(9) ? 1 : 0;
                    case 4 -> CargoCrateBlockEntity.this.canExtract(27) ? 1 : 0;
                    case 5 -> CargoCrateBlockEntity.this.canExtract(-1) ? 1 : 0;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                // NO-OP
            }

            public int size() {
                return 6;
            }
        };
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.nemuelch.cargo_crate");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CargoCrateScreenHandler(syncId, playerInventory, this.inventory,
                ScreenHandlerContext.create(this.world, this.pos), this.propertyDelegate);
    }

    public void setOriginalBlocksStacks(List<ItemStack> originalBlocksStacks) {
        for (int i = 0; i < originalBlocksStacks.size() && i < originalBlocks.size(); i++) {
            ItemStack blockStack = originalBlocksStacks.get(i);
            if (blockStack.isEmpty()) continue;
            this.originalBlocks.set(i, blockStack);
        }
    }

    public CargoCrateInventory getInventory() {
        return inventory;
    }

    public void dropInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.pos, this.getInventory().getStacks());
        ItemScatterer.spawn(serverWorld, this.pos, this.originalBlocks);
    }

    public boolean canExtract(int stackAmount) {
        return true;    //TODO: depends on inventory blocks nearby
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.inventory.readNbt(nbt.getCompound(NeMuelchNbtKeys.INVENTORY));

        if (nbt.contains(NeMuelchNbtKeys.ORIGINAL)) {
            NbtCompound inventoryNbt = nbt.getCompound(NeMuelchNbtKeys.ORIGINAL);
            Inventories.readNbt(inventoryNbt, this.originalBlocks);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        this.inventory.writeNbt(nbt);

        NbtCompound originalBlocksNbt = new NbtCompound();
        Inventories.writeNbt(originalBlocksNbt, this.originalBlocks);
        nbt.put(NeMuelchNbtKeys.ORIGINAL, originalBlocksNbt);
    }
}
