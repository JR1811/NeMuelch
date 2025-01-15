package net.shirojr.nemuelch.util.helper;

import net.minecraft.entity.Entity;
import net.shirojr.nemuelch.util.Attachable;
import org.jetbrains.annotations.Nullable;

public class AttachableHelper {
    public static void attachBoth(Attachable first, Attachable second) {
        first.nemuelch$setAttachedEntity(second.nemuelch$getUuid());
        second.nemuelch$setAttachedEntity(first.nemuelch$getUuid());
    }

    public static void detachBoth(@Nullable Attachable first, @Nullable Entity second) {
        if (first != null) {
            first.nemuelch$setAttachedEntity(null);
        }
        if (second instanceof Attachable attachableSecond) {
            attachableSecond.nemuelch$setAttachedEntity(null);
        }
    }
}
