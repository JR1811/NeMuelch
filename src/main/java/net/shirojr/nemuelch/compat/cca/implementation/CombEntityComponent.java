package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.util.constants.NbtKeys;
import org.jetbrains.annotations.NotNull;

public class CombEntityComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("combing");
    public static final int FULL_COMB_DURATION = 100;
    public static final int COOLDOWN = 30;

    private final LivingEntity entity;

    private boolean activeCombing;
    private int combTicks;
    private int cooldown;

    public CombEntityComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public static CombEntityComponent get(LivingEntity entity) {
        return NeMuelchComponents.COMBING_ENTITY.get(entity);
    }

    public boolean isActiveCombing() {
        return activeCombing;
    }

    private void setActiveCombing(boolean activeCombing) {
        this.activeCombing = activeCombing;
        this.sync();
    }


    @SuppressWarnings("UnusedReturnValue")
    public boolean startSession() {
        if (this.isActiveCombing() || this.isCooldownActive()) return false;
        this.setActiveCombing(true);
        return true;
    }

    public void stopSession() {
        this.setActiveCombing(false);

        if (getCombTicks() >= FULL_COMB_DURATION) {
            finishSession();
        } else {
            cancelSession();
        }
        this.setCooldown(COOLDOWN);
    }

    public int getCombTicks() {
        return combTicks;
    }

    private void setCombTicks(int combTicks) {
        this.combTicks = combTicks;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int combCooldown) {
        this.cooldown = MathHelper.clamp(combCooldown, 0, COOLDOWN);
    }

    public boolean isCooldownActive() {
        return this.getCooldown() > 0;
    }

    public void cancelSession() {
        this.entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 0, false, false, true));
        if (this.entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("info.nemuelch.comb_session.cancel"), true);
        }
    }

    public void finishSession() {
        if (this.entity.getRandom().nextFloat() < 0.2f) {
            this.entity.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 3, false, false, true));
        }
        if (this.entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("info.nemuelch.comb_session.success"), true);
        }
    }

    private void attemptCombSound() {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        if (entity.age % 50 != 0) return;
        if (serverWorld.getRandom().nextFloat() > 0.7) return;
        serverWorld.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                NeMuelchSounds.COMB, SoundCategory.NEUTRAL, 0.7f, 1f);
    }

    @Override
    public void serverTick() {
        if (isActiveCombing()) {
            this.setCombTicks(this.getCombTicks() + 1);
            this.attemptCombSound();
        }
        if (!isActiveCombing() && this.getCombTicks() > 0) {
            this.setCombTicks(0);
        }
        if (getCooldown() > 0) {
            this.setCooldown(this.getCooldown() - 1);
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        if (tag.contains(NbtKeys.ACTIVE_COMBING)) {
            this.activeCombing = tag.getBoolean(NbtKeys.ACTIVE_COMBING);
        }
        if (tag.contains(NbtKeys.COMB_TICKS)) {
            this.combTicks = tag.getInt(NbtKeys.COMB_TICKS);
        }
        if (tag.contains(NbtKeys.COOLDOWN)) {
            this.cooldown = tag.getInt(NbtKeys.COOLDOWN);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.putBoolean(NbtKeys.ACTIVE_COMBING, this.activeCombing);
        tag.putInt(NbtKeys.COMB_TICKS, this.combTicks);
        tag.putInt(NbtKeys.COOLDOWN, this.cooldown);
    }

    public void sync() {
        if (this.entity.getWorld().isClient()) return;
        NeMuelchComponents.COMBING_ENTITY.sync(this.entity);
    }
}
