package net.shirojr.nemuelch.compat.cca.implementation;

import com.mojang.authlib.GameProfile;
import dev.onyxstudios.cca.api.v3.component.Component;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LoginComponent implements Component {
    public static final Identifier KEY = NeMuelch.getId("login");

    private final Object2LongOpenHashMap<UUID> lastLogin;

    public LoginComponent() {
        this.lastLogin = new Object2LongOpenHashMap<>();
    }

    public static LoginComponent get(ServerWorld world) {
        return NeMuelchComponents.LOGIN.get(world.getScoreboard());
    }

    public static LoginComponent get(MinecraftServer server) {
        return NeMuelchComponents.LOGIN.get(server.getScoreboard());
    }

    public void setLogin(UUID entry) {
        this.lastLogin.put(entry, System.currentTimeMillis());
    }

    public boolean clearLogin(@Nullable UUID entry) {
        if (entry == null) {
            if (this.lastLogin.isEmpty()) return false;
            this.lastLogin.clear();
            return true;
        } else {
            boolean removedEntry = this.lastLogin.containsKey(entry);
            this.lastLogin.removeLong(entry);
            return removedEntry;
        }
    }

    public void clearAll() {
        this.lastLogin.clear();
    }

    public OptionalLong getLastLogin(UUID entry) {
        long time = this.lastLogin.getOrDefault(entry, -1L);
        return time == -1 ? OptionalLong.empty() : OptionalLong.of(time);
    }

    public List<Object2LongMap.Entry<UUID>> getSortedByLoginTime() {
        List<Object2LongMap.Entry<UUID>> result = this.lastLogin.object2LongEntrySet()
                .stream()
                .sorted(Comparator.comparingLong(Object2LongMap.Entry::getLongValue))
                .collect(Collectors.toList());
        Collections.reverse(result);
        return result;
    }

    public boolean isEmpty() {
        if (this.lastLogin.isEmpty()) return true;
        for (var entry : this.lastLogin.object2LongEntrySet()) {
            if (entry.getLongValue() != -1) return false;
        }
        return true;
    }

    public static String getFormattedTime(long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm z"));
    }

    public static String getCachedPlayerNameOrUuid(MinecraftServer server, UUID uuid) {
        return Optional.ofNullable(server.getUserCache()).flatMap(userCache -> userCache.getByUuid(uuid).map(GameProfile::getName)).orElse(uuid.toString());
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.lastLogin.clear();
        if (tag.contains(NeMuelchNbtKeys.LAST_LOGIN)) {
            NbtList entriesNbt = tag.getList(NeMuelchNbtKeys.LAST_LOGIN, NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < entriesNbt.size(); i++) {
                NbtCompound entryNbt = entriesNbt.getCompound(i);
                this.lastLogin.put(entryNbt.getUuid(NeMuelchNbtKeys.TARGET_UUID), entryNbt.getLong(NeMuelchNbtKeys.TIME));
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList listNbt = new NbtList();
        for (var entry : this.lastLogin.object2LongEntrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putUuid(NeMuelchNbtKeys.TARGET_UUID, entry.getKey());
            entryNbt.putLong(NeMuelchNbtKeys.TIME, entry.getLongValue());
            listNbt.add(entryNbt);
        }
        tag.put(NeMuelchNbtKeys.LAST_LOGIN, listNbt);
    }
}
