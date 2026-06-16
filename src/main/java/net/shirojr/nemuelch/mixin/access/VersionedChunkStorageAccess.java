package net.shirojr.nemuelch.mixin.access;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VersionedChunkStorage.class)
public interface VersionedChunkStorageAccess {
    @Accessor("worker")
    StorageIoWorker getWorker();
}
