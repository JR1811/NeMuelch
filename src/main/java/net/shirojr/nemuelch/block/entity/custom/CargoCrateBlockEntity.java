package net.shirojr.nemuelch.block.entity.custom;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.storage.CargoCrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchGameRules;
import net.shirojr.nemuelch.inventory.CargoCrateInventory;
import net.shirojr.nemuelch.screen.handler.CargoCrateScreenHandler;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class CargoCrateBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private static final int STACK_PER_BLOCK_COUNT = 27;
    public static final int ORIGINAL_BLOCKS_AMOUNT = 27;
    public static final int INVENTORY_STACKS_AMOUNT = (int) (STACK_PER_BLOCK_COUNT * ORIGINAL_BLOCKS_AMOUNT * 1.5);

    private final DefaultedList<ItemStack> originalBlocks;
    private final CargoCrateInventory inventory;
    private final Storage<ItemVariant> exposedStorage;
    private final PropertyDelegate propertyDelegate;
    private final HashMap<Direction, LinkedHashSet<BlockPos>> connectedNeighbors = new HashMap<>();

    private int tick = 0;
    private boolean isStructurePowered = false;
    private boolean powerDirty = true;

    public CargoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CARGO_CRATE, pos, state);
        this.originalBlocks = DefaultedList.ofSize(ORIGINAL_BLOCKS_AMOUNT, ItemStack.EMPTY);
        this.inventory = new CargoCrateInventory(INVENTORY_STACKS_AMOUNT, this::markDirty);
        this.exposedStorage = InventoryStorage.of(this.inventory, null);

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

    public void addConnectedNeighbor(BlockPos connectionInStructure, BlockPos inventoryNeighbor) {
        Direction direction = null;
        for (Direction directionEntry : Direction.values()) {
            if (connectionInStructure.offset(directionEntry).equals(inventoryNeighbor)) {
                direction = directionEntry;
                break;
            }
        }
        if (direction == null) {
            NeMuelch.LOGGER.warn("Tried to add a neighbor subscription to, non-connected Cargo Crate at: {}", connectionInStructure.toShortString());
            return;
        }
        LinkedHashSet<BlockPos> neighbors = this.connectedNeighbors.computeIfAbsent(direction, newEntry -> new LinkedHashSet<>());
        neighbors.add(inventoryNeighbor);
        this.markDirty();
        //TODO: make use of neighbors for insertion / extraction
    }

    public void removeConnectedNeighbor(BlockPos neighbor) {
        for (LinkedHashSet<BlockPos> neighbors : this.connectedNeighbors.values()) {
            neighbors.removeIf(neighborEntry -> neighborEntry.equals(neighbor));
        }
        this.cleanupConnectedNeighborList();
        this.markDirty();
    }

    public void cleanupConnectedNeighborList() {
        this.connectedNeighbors.entrySet().removeIf(entries -> entries.getValue().isEmpty());
    }

    public boolean isStructurePowered() {
        return isStructurePowered;
    }

    public void setStructurePowered(boolean structurePowered) {
        boolean old = this.isStructurePowered;
        this.isStructurePowered = structurePowered;
        if (old != this.isStructurePowered) {
            this.markDirty();
        }
    }

    public void markPowerDirty() {
        this.powerDirty = true;
    }

    public void serverTick(ServerWorld world, BlockPos pos, BlockState state) {
        int tickSpeed = world.getGameRules().getInt(NeMuelchGameRules.CARGO_CRATE_TICK_SPEED);
        if (tickSpeed <= 0 || ++this.tick < tickSpeed) return;
        this.tick = 0;

        if (this.powerDirty) {
            this.recomputeStructurePower(world, pos);
            this.powerDirty = false;
        }
        if (!this.isStructurePowered()) return;

        this.pushToSides(world);
        this.pullFromTop(world);
    }

    private void recomputeStructurePower(ServerWorld world, BlockPos pos) {
        List<CachedBlockPosition> connectedStructure = CargoCrateBlock.getConnectedStructure(world, pos);
        if (connectedStructure == null) return;
        boolean isAnyPowered = false;
        for (CachedBlockPosition entry : connectedStructure) {
            if (!world.isReceivingRedstonePower(entry.getBlockPos())) continue;
            isAnyPowered = true;
            break;
        }
        this.setStructurePowered(isAnyPowered);
    }

    private void pushToSides(ServerWorld world) {
        int moveAmount = world.getGameRules().getInt(NeMuelchGameRules.CARGO_CRATE_MOVE_AMOUNT);
        boolean anyMoved = false;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            LinkedHashSet<BlockPos> neighbors = this.connectedNeighbors.get(direction);
            if (neighbors == null) continue;
            for (BlockPos neighbor : neighbors) {
                if (!world.getChunkManager().isChunkLoaded(neighbor.getX() >> 4, neighbor.getZ() >> 4)) continue;
                Storage<ItemVariant> targetStorage = ItemStorage.SIDED.find(world, neighbor, direction.getOpposite());
                if (targetStorage == null) continue;
                if (this.moveOneNeighbor(this.exposedStorage, targetStorage, moveAmount, itemVariant -> true)) {
                    anyMoved = true;
                }
            }
            if (anyMoved) {
                world.playSound(null, this.pos, SoundEvents.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 2f, 0.8f);
            }
        }
    }

    private void pullFromTop(ServerWorld world) {
        int moveAmount = world.getGameRules().getInt(NeMuelchGameRules.CARGO_CRATE_MOVE_AMOUNT);
        Direction direction = Direction.UP;
        LinkedHashSet<BlockPos> neighbors = this.connectedNeighbors.get(direction);
        if (neighbors == null) return;
        boolean anyMoved = false;
        for (BlockPos neighbor : neighbors) {
            if (!world.getChunkManager().isChunkLoaded(neighbor.getX() >> 4, neighbor.getZ() >> 4)) continue;
            Storage<ItemVariant> targetStorage = ItemStorage.SIDED.find(world, neighbor, direction.getOpposite());
            if (targetStorage == null) continue;
            if (this.moveOneNeighbor(this.exposedStorage, targetStorage, moveAmount, this::isAllowedInsertion)) {
                anyMoved = true;
            }
        }
        if (anyMoved) {
            world.playSound(null, this.pos, SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 2f, 0.8f);
        }
    }

    private boolean moveOneNeighbor(Storage<ItemVariant> exposedStorage, Storage<ItemVariant> targetStorage, int maxAmount, Predicate<ItemVariant> filter) {
        boolean anyMoved = false;
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(exposedStorage, targetStorage, filter, maxAmount, transaction);
            if (moved > 0) {
                transaction.commit();
                this.markDirty();
                anyMoved = true;
            }
        }
        return anyMoved;
    }

    public boolean isAllowedInsertion(ItemVariant toBeInsertion) {
        return this.inventory.canInsert(toBeInsertion.toStack(1));
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

        this.connectedNeighbors.clear();
        if (nbt.contains(NeMuelchNbtKeys.NEIGHBORS)) {
            NbtList neighborsNbt = nbt.getList(NeMuelchNbtKeys.NEIGHBORS, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < neighborsNbt.size(); i++) {
                NbtCompound entryNbt = neighborsNbt.getCompound(i);
                Direction direction = Direction.byName(entryNbt.getString(NeMuelchNbtKeys.DIRECTION));
                if (direction == null) continue;

                LinkedHashSet<BlockPos> connected = new LinkedHashSet<>();
                NbtList connectedNbt = entryNbt.getList(NeMuelchNbtKeys.CONNECTED, NbtElement.LONG_TYPE);
                for (NbtElement nbtElement : connectedNbt) {
                    connected.add(BlockPos.fromLong(((NbtLong) nbtElement).longValue()));
                }
                this.connectedNeighbors.put(direction, connected);
            }
        }

        this.isStructurePowered = nbt.contains(NeMuelchNbtKeys.POWERED) && nbt.getBoolean(NeMuelchNbtKeys.POWERED);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        this.inventory.writeNbt(nbt);

        NbtCompound originalBlocksNbt = new NbtCompound();
        Inventories.writeNbt(originalBlocksNbt, this.originalBlocks);
        nbt.put(NeMuelchNbtKeys.ORIGINAL, originalBlocksNbt);

        this.cleanupConnectedNeighborList();
        NbtList neighborsNbt = new NbtList();
        for (var directionEntry : this.connectedNeighbors.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putString(NeMuelchNbtKeys.DIRECTION, directionEntry.getKey().getName());
            NbtList connectedNbt = new NbtList();
            for (BlockPos blockPos : directionEntry.getValue()) {
                connectedNbt.add(NbtLong.of(blockPos.asLong()));
            }
            entryNbt.put(NeMuelchNbtKeys.CONNECTED, connectedNbt);
            neighborsNbt.add(entryNbt);
        }
        nbt.put(NeMuelchNbtKeys.NEIGHBORS, neighborsNbt);

        nbt.putBoolean(NeMuelchNbtKeys.POWERED, this.isStructurePowered);
    }
}
