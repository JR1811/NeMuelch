package net.shirojr.nemuelch.compat.cca.util.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("unused")
public abstract class AbstractMonsterType implements MonsterTransitionCallback {
    protected final Identifier identifier;
    protected final LivingEntity provider;
    protected final float defaultDominance;

    protected float dominance;

    protected AbstractMonsterType(Identifier identifier, LivingEntity provider, float defaultDominance) {
        this.identifier = identifier;
        this.provider = provider;
        this.defaultDominance = MathHelper.clamp(defaultDominance, 0, 1);
        this.dominance = this.defaultDominance;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public LivingEntity getProvider() {
        return provider;
    }

    public float getDefaultDominance() {
        return defaultDominance;
    }

    public float getDominance() {
        return dominance;
    }

    public void setDominance(float dominance) {
        this.dominance = MathHelper.clamp(dominance, 0, 1);
    }

    public void resetDominance() {
        this.dominance = this.defaultDominance;
    }

    public abstract void serverTick();

    public void playSoundForProvider(SoundEvent sound, SoundCategory category, Vec3d pos, float volume, float pitch) {
        if (!(provider instanceof ServerPlayerEntity serverPlayer) || serverPlayer.networkHandler == null) return;
        serverPlayer.networkHandler.sendPacket(
                new PlaySoundS2CPacket(
                        Registries.SOUND_EVENT.getEntry(sound),
                        category,
                        pos.getX(), pos.getY(), pos.getZ(),
                        volume, pitch,
                        serverPlayer.age
                )
        );
    }

    public final NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("identifier", identifier.toString());
        nbt.putFloat("dominance", dominance);
        writeCustomNbt(nbt);
        return nbt;
    }

    public final void applyNbt(NbtCompound nbt) {
        // identifier is checked externally!
        if (nbt.contains("dominance")) {
            this.setDominance(nbt.getFloat("dominance"));
        }
        readCustomNbt(nbt);
    }

    /**
     * Override this method to write additional NBT data specific to your monster type.
     * The base identifier and dominance are already handled.
     *
     * @param nbt The NBT compound to write to
     */
    abstract protected void writeCustomNbt(NbtCompound nbt);

    /**
     * Override this method to read additional NBT data specific to your monster type.
     * The base identifier and dominance are already handled.
     *
     * @param nbt The NBT compound to read from
     */
    abstract  protected void readCustomNbt(NbtCompound nbt);
}
