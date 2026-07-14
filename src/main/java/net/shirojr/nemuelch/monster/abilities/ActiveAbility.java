package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.util.constants.NbtKeys;

public abstract class ActiveAbility implements Ability {
    private int cooldownDuration;
    private int cooldown;
    private boolean pauseCooldownTicking;

    protected ActiveAbility(int cooldownDuration) {
        this.cooldownDuration = cooldownDuration;
    }

    protected ActiveAbility() {
    }

    public void startCooldown() {
        this.cooldown = this.cooldownDuration;
    }

    public void clearCooldown() {
        this.cooldown = 0;
    }

    public boolean isOnCooldown() {
        return this.cooldown > 0;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldownDuration(int cooldownDuration) {
        this.cooldownDuration = Math.max(0, cooldownDuration);
    }

    public void pauseCooldown(boolean pause) {
        this.pauseCooldownTicking = pause;
    }

    public boolean isCooldownPaused() {
        return pauseCooldownTicking;
    }

    public void keybindInteraction(int index, ServerPlayerEntity user, boolean pressed) {
    }

    @Override
    public void tickServer(ServerPlayerEntity player) {
        if (this.cooldown > 0 && !this.pauseCooldownTicking) {
            this.cooldown = this.cooldown - 1;
        }
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
        if (nbt.contains(NbtKeys.COOLDOWN)) {
            this.cooldown = nbt.getInt(NbtKeys.COOLDOWN);
        }
        if (nbt.contains(NbtKeys.COOLDOWN_DURATION)) {
            this.cooldownDuration = nbt.getInt(NbtKeys.COOLDOWN_DURATION);
        }
        if (nbt.contains(NbtKeys.PAUSE_COOLDOWN)) {
            this.pauseCooldownTicking = nbt.getBoolean(NbtKeys.PAUSE_COOLDOWN);
        }
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        nbt.putInt(NbtKeys.COOLDOWN, this.cooldown);
        nbt.putInt(NbtKeys.COOLDOWN_DURATION, this.cooldownDuration);
        nbt.putBoolean(NbtKeys.PAUSE_COOLDOWN, this.pauseCooldownTicking);
    }
}
