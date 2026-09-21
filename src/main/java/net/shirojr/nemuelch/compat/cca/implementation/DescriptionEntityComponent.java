package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.util.DescriptionData;
import net.shirojr.nemuelch.network.packet.RequestDescribeClipboardS2CPacket;
import net.shirojr.nemuelch.network.util.PendingClipboardRequest;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DescriptionEntityComponent implements Component, AutoSyncedComponent, ServerTickingComponent {
    public static final Identifier KEY = NeMuelch.getId("description_entity");
    private static final int CLIPBOARD_PACKET_COOLDOWN = 100;
    private static final int PENDING_CLIPBOARD_REQUEST_TIMEOUT_DURATION = 600;

    private final LivingEntity provider;
    @Nullable
    private DescriptionData dataSelf;
    private boolean preventDescriptionRendering;    // toggle for disabling rendering of all other data

    private int clipboardPacketCooldown;
    private @Nullable PendingClipboardRequest pendingClipboardRequest;
    private int pendingClipboardTimeout;

    public DescriptionEntityComponent(LivingEntity provider) {
        this.provider = provider;
        this.preventDescriptionRendering = false;
    }

    public static DescriptionEntityComponent get(LivingEntity provider) {
        return NeMuelchComponents.DESCRIPTION_ENTITY.get(provider);
    }

    public @Nullable DescriptionData getDataSelf() {
        return this.dataSelf;
    }

    public void setData(@Nullable DescriptionData data, boolean shouldSync) {
        this.dataSelf = data;
        if (shouldSync) {
            this.sync();
        }
    }

    public boolean hidesAllDescriptions() {
        return preventDescriptionRendering;
    }

    public void setHideAllDescriptions(boolean hideAllDescriptions, boolean shouldSync) {
        this.preventDescriptionRendering = hideAllDescriptions;
        if (shouldSync) this.sync();
    }

    public boolean isOnClipboardPacketCooldown() {
        return this.clipboardPacketCooldown > 0;
    }

    public int getClipboardPacketCooldown() {
        return this.clipboardPacketCooldown;
    }

    public void startClipboardPacketCooldown() {
        this.clipboardPacketCooldown = CLIPBOARD_PACKET_COOLDOWN;
    }

    public void requestClipboardFromClient(int descriptionDuration, int maxSymbolLength, @Nullable List<UUID> descriptionTargets) {
        if (!(this.provider instanceof ServerPlayerEntity player)) return;
        this.pendingClipboardRequest = new PendingClipboardRequest(descriptionDuration, descriptionTargets);
        this.pendingClipboardTimeout = PENDING_CLIPBOARD_REQUEST_TIMEOUT_DURATION;
        new RequestDescribeClipboardS2CPacket(maxSymbolLength).sendPacket(List.of(player));
    }

    public @Nullable PendingClipboardRequest consumePendingClipboardRequest() {
        PendingClipboardRequest request = this.pendingClipboardRequest;
        this.pendingClipboardRequest = null;
        return request;
    }

    @Override
    public void serverTick() {
        if (this.dataSelf != null) {
            if (this.dataSelf.isFinished()) this.setData(null, true);
            else this.dataSelf.decrementDuration();
        }
        if (this.clipboardPacketCooldown > 0) {
            this.clipboardPacketCooldown --;
            if (this.clipboardPacketCooldown == 0) {
                MinecraftServer server = this.provider.getServer();
                if (server != null) {
                    server.sendMessage(Text.literal("Clipboard packet cooldown for entity describe data finished"));
                }
            }
        }
        if (this.pendingClipboardRequest != null && --this.pendingClipboardTimeout <= 0) {
            this.pendingClipboardRequest = null;
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        if (tag.contains(NeMuelchNbtKeys.DESCRIPTION_DATA)) {
            this.setData(DescriptionData.fromNbt(tag.getCompound(NeMuelchNbtKeys.DESCRIPTION_DATA)), false);
        } else if (this.getDataSelf() != null) {
            this.setData(null, false);
        }

        if (tag.contains(NeMuelchNbtKeys.HIDE)) {
            this.setHideAllDescriptions(tag.getBoolean(NeMuelchNbtKeys.HIDE), false);
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        if (this.getDataSelf() != null) {
            NbtCompound dataNbt = new NbtCompound();
            this.getDataSelf().toNbt(dataNbt);
            tag.put(NeMuelchNbtKeys.DESCRIPTION_DATA, dataNbt);
        }
        tag.putBoolean(NeMuelchNbtKeys.HIDE, this.hidesAllDescriptions());
    }

    public void sync() {
        if (!(this.provider.getWorld() instanceof ServerWorld)) return;
        NeMuelchComponents.DESCRIPTION_ENTITY.sync(this.provider);
    }
}
