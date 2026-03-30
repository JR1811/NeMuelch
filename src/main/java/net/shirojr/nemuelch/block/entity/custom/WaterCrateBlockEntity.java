package net.shirojr.nemuelch.block.entity.custom;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.util.data.EntityStorageEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class WaterCrateBlockEntity extends BlockEntity {
    public static final long MAX_CAPACITY = FluidConstants.BUCKET * 10;

    @Nullable
    private EntityStorageEntry storedEntity;
    private int storedEntityDuration;

    private final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return MAX_CAPACITY;
        }

        @Override
        protected void onFinalCommit() {
            WaterCrateBlockEntity.this.markDirty();
        }
    };

    public WaterCrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.WATER_CRATE, pos, state);
    }

    public @Nullable EntityStorageEntry getStoredEntity() {
        return storedEntity;
    }

    public boolean hasStoredEntity() {
        return this.getStoredEntity() != null;
    }

    public void setStoredEntity(@Nullable EntityStorageEntry storedEntity) {
        this.storedEntity = storedEntity;
        markDirty();
        if (storedEntity != null) this.startStoredEntityDuration();
        else this.stopStoredEntityDuration();
    }

    public int getStoredEntityDuration() {
        return storedEntityDuration;
    }

    public void setStoredEntityDuration(int storedEntityDuration) {
        this.storedEntityDuration = Math.max(storedEntityDuration, -1);
    }

    public void startStoredEntityDuration() {
        this.storedEntityDuration = 0;
    }

    public void stopStoredEntityDuration() {
        this.storedEntityDuration = -1;
    }

    @NotNull
    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("FluidVariant")) {
            this.fluidStorage.variant = FluidVariant.fromNbt(nbt.getCompound("FluidVariant"));
        }
        if (nbt.contains("FluidAmount")) {
            fluidStorage.amount = nbt.getLong("FluidAmount");
        }

        if (nbt.contains("Entity")) {
            this.storedEntity = EntityStorageEntry.fromNbt(nbt.getCompound("Entity"));
        } else {
            this.storedEntity = null;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("FluidVariant", this.fluidStorage.variant.toNbt());
        nbt.putLong("FluidAmount", this.fluidStorage.amount);

        if (this.storedEntity == null) {
            nbt.remove("Entity");
        } else {
            NbtCompound storedEntityNbt = new NbtCompound();
            this.storedEntity.toNbt(storedEntityNbt);
            nbt.put("Entity", storedEntityNbt);
        }
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
            serverWorld.updateListeners(getPos(), getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, WaterCrateBlockEntity blockEntity) {
        if (blockEntity.getStoredEntityDuration() != -1) {
            blockEntity.setStoredEntityDuration(blockEntity.getStoredEntityDuration() + 1);
        }
    }
}
