package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiscGlobalComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("misc_global");

    private final Scoreboard provider;

    @SuppressWarnings("unused")
    public MiscGlobalComponent(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.provider = scoreboard;
    }

    public Scoreboard getProvider() {
        return provider;
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound nbt) {

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound nbt) {

    }
}
