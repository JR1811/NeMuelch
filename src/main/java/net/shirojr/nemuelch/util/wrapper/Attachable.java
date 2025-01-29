package net.shirojr.nemuelch.util.wrapper;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.util.helper.AttachableHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface Attachable {
    Optional<UUID> nemuelch$getAttachedEntity();

    void nemuelch$setAttachedEntity(@Nullable UUID attachedUuid);

    default boolean nemuelch$isAttached() {
        return nemuelch$getAttachedEntity().isPresent();
    }

    UUID nemuelch$getUuid();

    default void nemuelch$snap(ServerWorld world, @Nullable UUID other) {
        AttachableHelper.detachBoth(this, world.getEntity(other));
        if (this instanceof Entity own) {
            Vec3d pos = own.getPos();
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 2f, 1f);
        }
    }
}
