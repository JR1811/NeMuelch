package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpikeTrapBlockEntity extends BlockEntity {
    @Nullable
    private NbtCompound potionNbt;

    public SpikeTrapBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.SPIKE_TRAP, pos, state);
        this.potionNbt = null;
    }

    @Nullable
    public Potion getPotion() {
        Potion potion = PotionUtil.getPotion(this.potionNbt);
        if (potion == null || potion.equals(Potions.EMPTY)) return null;
        return potion;
    }

    public void setPotion(@Nullable Potion potion) {
        if (potion == null || potion.equals(Potions.EMPTY)) this.potionNbt = null;
        Identifier identifier = Registries.POTION.getId(potion);
        if (identifier.equals(Registries.POTION.getDefaultId())) {
            NeMuelch.LOGGER.warn("Potion not found in registry: {}", identifier);
            return;
        }
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Potion", identifier.toString());
        this.potionNbt = nbt;
        this.markDirty();
    }

    public boolean hasPotion() {
        return this.potionNbt != null && !this.potionNbt.isEmpty();
    }

    public boolean canApplyPotion(@Nullable Potion potion) {
        if (potion == null || !this.hasPotion()) return true;
        return !Objects.equals(PotionUtil.getPotion(this.potionNbt), potion);
    }

    public void clear() {
        this.setPotion(null);
    }

    public void applyEffects(LivingEntity entity) {
        Potion potion = this.getPotion();
        if (potion == null) return;
        potion.getEffects().forEach(entity::addStatusEffect);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(NeMuelchNbtKeys.POTION)) {
            this.potionNbt = nbt.getCompound(NeMuelchNbtKeys.POTION);
        } else {
            this.potionNbt = null;
        }
        if (world != null && world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.hasPotion()) {
            nbt.put(NeMuelchNbtKeys.POTION, this.potionNbt);
        } else {
            nbt.remove(NeMuelchNbtKeys.POTION);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().markForUpdate(getPos());
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
