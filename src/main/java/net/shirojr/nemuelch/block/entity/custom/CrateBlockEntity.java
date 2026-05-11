package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import net.shirojr.nemuelch.util.data.EntityStorageEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrateBlockEntity extends BlockEntity {
    private final SimpleInventory topInventory = new SimpleInventory(6);
    private final SimpleInventory bottomInventory = new SimpleInventory(6);
    private ItemStack standStack;

    @Nullable
    private EntityStorageEntry storedEntity;
    private long storedEntityDuration;


    public CrateBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.CRATE, pos, state);
        this.standStack = ItemStack.EMPTY;
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

    @NotNull
    public ItemStack getStandStack() {
        return standStack;
    }

    public void setStandStack(@NotNull ItemStack standStack) {
        this.standStack = standStack;
        markDirty();
    }

    public void releaseStandStack() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            ItemScatterer.spawn(serverWorld, pos.getX(), pos.getY(), pos.getZ(), this.getStandStack().copy());
        }
        this.setStandStack(ItemStack.EMPTY);
    }

    public boolean hasStandStack() {
        return !this.standStack.equals(ItemStack.EMPTY);
    }

    public boolean canAddItem(SimpleInventory inventory, ItemStack toBeAdded) {
        if (inventory == null || hasStoredEntity()) return false;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) return true;
            if (stack.getCount() == stack.getMaxCount()) continue;
            if (ItemStack.canCombine(stack, toBeAdded)) return true;
        }
        return false;
    }

    public @Nullable EntityStorageEntry getStoredEntity() {
        return storedEntity;
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

    public boolean canAddEntity(Entity entity) {
        if (!entity.getWorld().getGameRules().getBoolean(NemuelchGameRules.CRATE_STORES_ENTITIES)) return false;
        if (getCachedState().get(CrateBlock.TYPE) == CrateBlock.Type.DOUBLE) return false;
        if (hasStoredEntity()) return false;
        if (entity instanceof Tameable tameable && tameable.getOwner() == null) return false;
        return !entity.getType().isIn(NeMuelchTags.EntityTypes.CRATE_STORAGE_BLACKLIST);
    }

    public void setStoredEntity(@Nullable Entity entity, boolean discardEntity) {
        if (entity == null) {
            this.storedEntity = null;
            this.stopStoredEntityDuration();
        } else {
            this.storedEntity = EntityStorageEntry.create(entity);
            this.startStoredEntityDuration();
            if (discardEntity) {
                entity.discard();
            }
        }
        this.markDirty();
    }

    public boolean hasStoredEntity() {
        return this.storedEntity != null;
    }

    public void addStoredEntity(Entity toBeAdded) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        if (!canAddEntity(toBeAdded)) return;
        serverWorld.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS);
        serverWorld.spawnParticles(ParticleTypes.CLOUD,
                toBeAdded.getBlockPos().toCenterPos().getX(),
                toBeAdded.getBlockPos().toCenterPos().getY(),
                toBeAdded.getBlockPos().toCenterPos().getZ(),
                10, 1, 1, 1, 0.01);
        if (toBeAdded instanceof MobEntity mobEntity) {
            mobEntity.detachLeash(true, serverWorld.getGameRules().getBoolean(GameRules.DO_TILE_DROPS));
        }
        this.setStoredEntity(toBeAdded, true);
        this.releaseBottomInventory();
        this.releaseTopInventory();
        CrateBlock.changeType(serverWorld, getPos(), CrateBlock.Type.ENTITY);
    }

    public void releaseStoredEntity(World world, Vec3d spawnPos, @Nullable Entity leashHolder, @Nullable ItemStack leashStack) {
        if (!(world instanceof ServerWorld serverWorld) || this.storedEntity == null) return;
        Entity entity = this.storedEntity.spawn(serverWorld, spawnPos);
        if (entity == null) return;
        if (leashHolder != null && entity instanceof MobEntity mobEntity) {
            mobEntity.attachLeash(leashHolder, world instanceof ServerWorld);
        }
        this.setStoredEntity(null, false);
        BlockPos effectPos = this.getPos();
        serverWorld.playSound(null, effectPos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS);
        serverWorld.spawnParticles(ParticleTypes.CLOUD,
                effectPos.toCenterPos().getX(), effectPos.toCenterPos().getY(), effectPos.toCenterPos().getZ(),
                10, 1, 1, 1, 0.01);
        if (leashStack != null && leashHolder instanceof PlayerEntity player && !player.isCreative()) {
            leashStack.decrement(1);
        }
        CrateBlock.changeType(serverWorld, getPos(), CrateBlock.Type.SINGLE);
        markDirty();
    }

    public void onBroken() {
        this.releaseTopInventory();
        this.releaseBottomInventory();
        this.releaseStoredEntity(getWorld(), this.getPos().toCenterPos(), null, null);
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

        if (nbt.contains("Entity")) {
            this.storedEntity = EntityStorageEntry.fromNbt(nbt.getCompound("Entity"));
        } else {
            this.storedEntity = null;
        }

        if (nbt.contains("StoredEntityDuration")) {
            this.setStoredEntityDuration(nbt.getLong("StoredEntityDuration"));
        }

        if (nbt.contains("StandStack")) {
            this.setStandStack(ItemStack.fromNbt(nbt.getCompound("StandStack")));
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

        if (this.storedEntity == null) {
            nbt.remove("Entity");
        } else {
            NbtCompound storedEntityNbt = new NbtCompound();
            this.storedEntity.toNbt(storedEntityNbt);
            nbt.put("Entity", storedEntityNbt);
        }

        nbt.putLong("StoredEntityDuration", this.getStoredEntityDuration());

        NbtCompound standStackNbt = new NbtCompound();
        this.getStandStack().writeNbt(standStackNbt);
        nbt.put("StandStack", standStackNbt);
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
        if (blockEntity.getStoredEntityDuration() != -1) {
            blockEntity.setStoredEntityDuration(blockEntity.getStoredEntityDuration() + 1);
        }
    }
}
