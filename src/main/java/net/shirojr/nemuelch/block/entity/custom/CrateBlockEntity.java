package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchTags;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CrateBlockEntity extends BlockEntity {
    private final SimpleInventory topInventory = new SimpleInventory(6);
    private final SimpleInventory bottomInventory = new SimpleInventory(6);

    @Nullable
    private EntityType<?> storedEntityType;
    @Nullable
    private NbtCompound storedEntityDataNbt;


    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CRATE, pos, state);
        this.storedEntityType = null;
    }

    public SimpleInventory getBottomInventory() {
        return bottomInventory;
    }

    public void releaseBottomInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.getPos().up(), this.getTopInventory());
        this.bottomInventory.clear();
        markDirty();
    }

    public SimpleInventory getTopInventory() {
        return topInventory;
    }

    public void releaseTopInventory() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        ItemScatterer.spawn(serverWorld, this.getPos().up(), this.getTopInventory());
        this.topInventory.clear();
        markDirty();
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

    public boolean canAddItems() {
        return this.storedEntityType == null && this.storedEntityDataNbt == null;
    }

    public boolean canAddEntity(MobEntity entity) {
        if (this.storedEntityType != null || this.storedEntityDataNbt != null) return false;
        return !entity.getType().isIn(NeMuelchTags.EntityTypes.CRATE_STORAGE_BLACKLIST);
    }

    public void setStoredEntity(@Nullable MobEntity entity, boolean discardEntity) {
        if (entity == null) {
            this.storedEntityType = null;
            this.storedEntityDataNbt = null;
            return;
        }
        this.storedEntityType = entity.getType();
        this.storedEntityDataNbt = entity.writeNbt(new NbtCompound());

        if (discardEntity) {
            entity.discard();
        }
    }

    @Nullable
    public MobEntity createStoredEntity(World world) {
        if (this.storedEntityType == null || this.storedEntityDataNbt == null) return null;
        if (!(this.storedEntityType.create(world) instanceof MobEntity mobEntity)) return null;
        mobEntity.readNbt(this.storedEntityDataNbt);
        mobEntity.setUuid(UUID.randomUUID());
        return mobEntity;
    }

    public void spawnStoredEntity(Vec3d pos) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        MobEntity entity = this.createStoredEntity(serverWorld);
        if (entity == null) return;
        serverWorld.spawnEntity(entity);
        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
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
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
