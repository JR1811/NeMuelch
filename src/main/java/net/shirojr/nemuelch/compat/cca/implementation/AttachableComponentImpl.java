package net.shirojr.nemuelch.compat.cca.implementation;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.component.AttachableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AttachableComponentImpl implements AttachableComponent, AutoSyncedComponent {
    private final Entity provider;

    private @Nullable Entity attachedEntity;

    public AttachableComponentImpl(Entity provider) {
        this.provider = provider;
    }

    @Override
    public Entity getProvider() {
        return this.provider;
    }

    @Override
    public @Nullable Entity getAttachedEntity() {
        return attachedEntity;
    }

    @Override
    public void setAttachedEntity(@Nullable Entity attachedEntity) {
        this.attachedEntity = attachedEntity;
    }

    @Override
    public Entity getSelf() {
        return provider;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (!(provider.getWorld() instanceof ServerWorld serverWorld)) return;
        if (nbt.contains("attached")) {
            setAttachedEntity(serverWorld.getEntity(nbt.getUuid("attached")));
        } else {
            setAttachedEntity(null);
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        if (!(provider.getWorld() instanceof ServerWorld)) return;
        if (getAttachedEntity() == null) {
            nbt.remove("attached");
        } else {
            nbt.putUuid("attached", getAttachedEntity().getUuid());
        }
    }

    @Override
    public void sync() {
        NeMuelchComponents.ATTACHABLE.sync(this.provider);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        setAttachedEntity(buf.readOptional(PacketByteBuf::readVarInt).map(integer -> provider.getWorld().getEntityById(integer)).orElse(null));
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeOptional(Optional.ofNullable(attachedEntity).map(Entity::getId), PacketByteBuf::writeVarInt);
    }
}
