package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.compat.cca.component.ActCommandComponent;

public class ActCommandComponentImpl implements ActCommandComponent, AutoSyncedComponent {
    private final PlayerEntity provider;
    private boolean enabledStalkMode;

    public ActCommandComponentImpl(PlayerEntity provider) {
        this.provider = provider;
    }

    public PlayerEntity getProvider() {
        return provider;
    }

    @Override
    public boolean enabledStalkMode() {
        return enabledStalkMode;
    }

    @Override
    public void setStalkMode(boolean enableStalk) {
        this.enabledStalkMode = enableStalk;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (tag.contains("stalk")) {
            this.setStalkMode(tag.getBoolean("stalk"));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("stalk", enabledStalkMode());
    }

    @SuppressWarnings("unused")
    public static void onRespawn(ActCommandComponentImpl from, ActCommandComponentImpl to, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        to.setStalkMode(from.enabledStalkMode());
    }
}
