package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CrateBlockEntity extends BlockEntity {
    private final SimpleInventory topInventory = new SimpleInventory(6);
    private final SimpleInventory bottomInventory = new SimpleInventory(6);

    @Nullable
    private EntityType<?> storedEntityType;
    @Nullable
    private NbtCompound storedEntityDataNbt;

    private long storedEntityDuration;


    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CRATE, pos, state);
        this.storedEntityType = null;
    }

    public SimpleInventory getBottomInventory() {
        return bottomInventory;
    }

    public void releaseBottomInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ItemScatterer.spawn(serverWorld, this.getPos(), this.bottomInventory);
        }
        this.bottomInventory.clear();
        markDirty();
    }

    @Nullable
    public SimpleInventory getTopInventory() {
        if (getCachedState().get(CrateBlock.TYPE) != CrateBlock.Type.DOUBLE) return null;
        return topInventory;
    }

    public void releaseTopInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ItemScatterer.spawn(serverWorld, this.getPos(), this.topInventory);
        }
        this.topInventory.clear();
        markDirty();
    }

    public SimpleInventory getInventory(@Nullable Vec3d hitPos) {
        BlockState cachedState = getCachedState();
        CrateBlock.Type type = cachedState.get(CrateBlock.TYPE);
        if (type != CrateBlock.Type.DOUBLE || hitPos == null) {
            return getBottomInventory();
        }
        return hitPos.getY() <= 0.5 ? getBottomInventory() : getTopInventory();
    }

    public boolean canAddItem(SimpleInventory inventory, ItemStack toBeAdded) {
        if (this.storedEntityType != null) return false;
        if (this.storedEntityDataNbt != null) return false;
        if (inventory == null) return false;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) return true;
            if (stack.getCount() == stack.getMaxCount()) continue;
            if (ItemStack.canCombine(stack, toBeAdded)) return true;
        }
        return false;
    }

    public @Nullable EntityType<?> getStoredEntityType() {
        return storedEntityType;
    }

    public @Nullable NbtCompound getStoredEntityDataNbt() {
        return storedEntityDataNbt;
    }

    public long getStoredEntityDuration() {
        return storedEntityDuration;
    }

    public void setStoredEntityDuration(long storedEntityDuration) {
        this.storedEntityDuration = Math.max(storedEntityDuration, -1);
    }

    public void startStoredEntityDuration() {
        this.storedEntityDuration = 0;
    }

    public void stopStoredEntityDuration() {
        this.storedEntityDuration = -1;
    }

    public boolean canAddEntity(MobEntity entity) {
        if (!entity.getWorld().getGameRules().getBoolean(NemuelchGameRules.CRATE_STORES_ENTITIES)) return false;
        if (this.storedEntityType != null || this.storedEntityDataNbt != null) return false;
        return entity.getType().isIn(NeMuelchTags.EntityTypes.CRATE_STORAGE_WHITELIST);
    }

    public void setStoredEntity(@Nullable MobEntity entity, boolean discardEntity) {
        if (entity == null) {
            this.storedEntityType = null;
            this.storedEntityDataNbt = null;
            this.stopStoredEntityDuration();
        } else {
            this.storedEntityType = entity.getType();
            this.storedEntityDataNbt = entity.writeNbt(new NbtCompound());
            this.startStoredEntityDuration();

            if (discardEntity) {
                entity.discard();
            }
        }
        this.markDirty();
    }

    @Nullable
    public MobEntity createStoredEntity(World world) {
        if (this.storedEntityType == null || this.storedEntityDataNbt == null) return null;
        if (!(this.storedEntityType.create(world) instanceof MobEntity mobEntity)) return null;
        mobEntity.readNbt(this.storedEntityDataNbt);
        mobEntity.setUuid(UUID.randomUUID());
        return mobEntity;
    }

    @Nullable
    public MobEntity spawnStoredEntity(Vec3d pos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return null;
        MobEntity entity = this.createStoredEntity(serverWorld);
        if (entity == null) return null;
        entity.setPosition(pos);
        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
        serverWorld.spawnEntity(entity);
        markDirty();
        return entity;
    }

    public void onBroken() {
        this.releaseTopInventory();
        this.releaseBottomInventory();
        this.spawnStoredEntity(this.getPos().toCenterPos());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.bottomInventory.clear();
        if (nbt.contains("BottomInventory")) {
            Inventories.readNbt(nbt.getCompound("BottomInventory"), bottomInventory.stacks);
        }

        this.topInventory.clear();
        if (nbt.contains("TopInventory")) {
            Inventories.readNbt(nbt.getCompound("TopInventory"), topInventory.stacks);
        }

        if (nbt.contains("StoredEntity")) {
            NbtCompound storedEntityNbt = nbt.getCompound("StoredEntity");
            EntityType.get(storedEntityNbt.getString("Type")).ifPresent(type -> {
                this.storedEntityType = type;
                this.storedEntityDataNbt = storedEntityNbt.getCompound("Data");
            });
        } else {
            this.storedEntityType = null;
            this.storedEntityDataNbt = null;
        }

        if (nbt.contains("StoredEntityDuration")) {
            this.setStoredEntityDuration(nbt.getLong("StoredEntityDuration"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        NbtCompound bottomInventoryNbt = new NbtCompound();
        Inventories.writeNbt(bottomInventoryNbt, bottomInventory.stacks);
        nbt.put("BottomInventory", bottomInventoryNbt);

        NbtCompound topInventoryNbt = new NbtCompound();
        Inventories.writeNbt(topInventoryNbt, topInventory.stacks);
        nbt.put("TopInventory", topInventoryNbt);

        if (this.storedEntityType == null || this.storedEntityDataNbt == null) {
            nbt.remove("StoredEntity");
        } else {
            NbtCompound storedEntityNbt = new NbtCompound();

            Identifier entityId = EntityType.getId(this.storedEntityType);
            if (entityId != null) {
                storedEntityNbt.putString("Type", entityId.toString());
                storedEntityNbt.put("Data", this.storedEntityDataNbt);
                nbt.put("StoredEntity", storedEntityNbt);
            }
        }

        nbt.putLong("StoredEntityDuration", this.getStoredEntityDuration());
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

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, CrateBlockEntity blockEntity) {
        if (blockEntity.getStoredEntityDuration() == -1) return;
        blockEntity.setStoredEntityDuration(blockEntity.getStoredEntityDuration() + 1);
    }
}
