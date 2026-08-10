package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.block.custom.SpikeTrapBlock;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

public class SpikeTrapBlockEntity extends BlockEntity {
    @Nullable
    private NbtCompound potionNbt;
    private int charges;

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

    public void addPotion(@Nullable Potion potion, int charges) {
        if (potion == null || potion.equals(Potions.EMPTY) || charges <= 0) {
            this.potionNbt = null;
            if (this.charges > 0) {
                this.charges = 0;
            }
        } else {
            Identifier identifier = Registries.POTION.getId(potion);
            if (identifier.equals(Registries.POTION.getDefaultId())) {
                NeMuelch.LOGGER.warn("Potion not found in registry: {}", identifier);
                return;
            }
            if (this.potionNbt != null && this.potionNbt.getString(NeMuelchNbtKeys.POTION).equals(identifier.toString())) {
                this.charges += charges;
            } else {
                NbtCompound nbt = new NbtCompound();
                nbt.putString(NeMuelchNbtKeys.POTION, identifier.toString());
                this.potionNbt = nbt;
                this.charges = Math.max(0, this.charges + charges);
            }
        }

        this.markDirty();
    }

    public void clearPotionWithSound() {
        this.clear();
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, NeMuelchSounds.SQUIRT, SoundCategory.BLOCKS, 1f, 0.8f);
            if (SpikeTrapBlock.State.isExposed(this.getCachedState())) {
                serverWorld.setBlockState(this.pos, this.getCachedState().with(SpikeTrapBlock.STATE, SpikeTrapBlock.State.EXPOSED), Block.NOTIFY_LISTENERS);
            }
        }
    }

    public void decrementCharge() {
        if (this.charges <= 0) return;
        this.charges--;
        if (this.charges <= 0) {
            this.clearPotionWithSound();
        }
        this.markDirty();
    }

    public boolean hasPotion() {
        return this.potionNbt != null && !this.potionNbt.isEmpty();
    }

    @SuppressWarnings("unused")
    public boolean canApplyPotion(@Nullable Potion potion) {
        return true;
    }

    public void clear() {
        this.addPotion(null, 0);
    }

    public void applyEffects(LivingEntity entity) {
        if (entity.getWorld().isClient()) return;
        Potion potion = this.getPotion();
        if (potion == null) return;
        potion.getEffects().forEach(instance -> entity.addStatusEffect(new StatusEffectInstance(instance)));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(NeMuelchNbtKeys.POTION)) {
            this.potionNbt = nbt.getCompound(NeMuelchNbtKeys.POTION);
        } else {
            this.potionNbt = null;
        }

        if (nbt.contains(NeMuelchNbtKeys.CHARGES)) {
            this.charges = nbt.getInt(NeMuelchNbtKeys.CHARGES);
        } else {
            this.charges = 0;
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

        nbt.putInt(NeMuelchNbtKeys.CHARGES, this.charges);
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
