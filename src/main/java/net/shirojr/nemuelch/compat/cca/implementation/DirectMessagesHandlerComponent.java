package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DirectMessagesHandlerComponent implements Component, AutoSyncedComponent {
    public static final Identifier KEY = NeMuelch.getId("direct_messages_handler");
    private final PlayerEntity player;

    private boolean blocksAllMessages;
    private final List<UUID> blockedTargets;

    public DirectMessagesHandlerComponent(PlayerEntity player) {
        this.player = player;
        this.blockedTargets = new ArrayList<>();
    }

    public static DirectMessagesHandlerComponent get(PlayerEntity player) {
        return NeMuelchComponents.DIRECT_MESSAGE_HANDLER.get(player);
    }

    public boolean blocksAllMessages() {
        return blocksAllMessages;
    }

    public void setBlocksAllMessages(boolean blocksAllMessages) {
        this.blocksAllMessages = blocksAllMessages;
        this.sync();
    }

    public List<UUID> getBlockedTargets() {
        return Collections.unmodifiableList(this.blockedTargets);
    }

    public void modifyBlockedTargets(Consumer<List<UUID>> targetsModifier) {
        List<UUID> old = new ArrayList<>(this.blockedTargets);
        targetsModifier.accept(this.blockedTargets);
        if (!old.equals(this.blockedTargets)) {
            this.sync();
        }
    }

    public boolean isBlocked(PlayerEntity target) {
        return this.blocksAllMessages() || this.blockedTargets.contains(target.getUuid());
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        this.blocksAllMessages = tag.getBoolean(NeMuelchNbtKeys.BLOCKS_ALL_MESSAGES);

        if (tag.contains(NeMuelchNbtKeys.BLOCKED_TARGETS)) {
            this.blockedTargets.clear();
            NbtList entriesNbt = tag.getList(NeMuelchNbtKeys.BLOCKED_TARGETS, NbtElement.STRING_TYPE);
            for (int i = 0; i < entriesNbt.size(); i++) {
                this.blockedTargets.add(UUID.fromString(entriesNbt.getString(i)));
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        tag.putBoolean(NeMuelchNbtKeys.BLOCKS_ALL_MESSAGES, this.blocksAllMessages);

        NbtList entriesNbt = new NbtList();
        for (UUID blockedTarget : this.blockedTargets) {
            entriesNbt.add(NbtString.of(blockedTarget.toString()));
        }
        tag.put(NeMuelchNbtKeys.BLOCKED_TARGETS, entriesNbt);
    }

    @SuppressWarnings("unused")
    public static void onRespawn(DirectMessagesHandlerComponent from, DirectMessagesHandlerComponent to, boolean lossless,
                                 boolean keepInventory, boolean sameCharacter) {
        to.blocksAllMessages = from.blocksAllMessages;
        to.blockedTargets.addAll(from.blockedTargets);
    }

    public void sync() {
        if (!(this.player instanceof ServerPlayerEntity)) return;
        NeMuelchComponents.DIRECT_MESSAGE_HANDLER.sync(this.player);
    }
}
