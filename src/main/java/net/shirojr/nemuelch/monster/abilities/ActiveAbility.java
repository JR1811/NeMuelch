package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;

public abstract class ActiveAbility implements Ability {
    private int cooldownDuration;
    private int cooldown;
    private boolean pauseCooldownTicking;

    protected ActiveAbility(int cooldownDuration) {
        this.setCooldownDuration(cooldownDuration);
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
        if (nbt.contains(NeMuelchNbtKeys.COOLDOWN)) {
            this.cooldown = nbt.getInt(NeMuelchNbtKeys.COOLDOWN);
        }
        if (nbt.contains(NeMuelchNbtKeys.COOLDOWN_DURATION)) {
            this.cooldownDuration = nbt.getInt(NeMuelchNbtKeys.COOLDOWN_DURATION);
        }
        if (nbt.contains(NeMuelchNbtKeys.PAUSE_COOLDOWN)) {
            this.pauseCooldownTicking = nbt.getBoolean(NeMuelchNbtKeys.PAUSE_COOLDOWN);
        }
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        nbt.putInt(NeMuelchNbtKeys.COOLDOWN, this.cooldown);
        nbt.putInt(NeMuelchNbtKeys.COOLDOWN_DURATION, this.cooldownDuration);
        nbt.putBoolean(NeMuelchNbtKeys.PAUSE_COOLDOWN, this.pauseCooldownTicking);
    }
}
