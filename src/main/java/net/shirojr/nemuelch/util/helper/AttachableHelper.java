package net.shirojr.nemuelch.util.helper;

import net.shirojr.nemuelch.util.Attachable;
import org.jetbrains.annotations.Nullable;

public class AttachableHelper {
    public static void attachBoth(Attachable first, Attachable second) {
        first.nemuelch$setAttachedEntity(second.nemuelch$getUuid());
        second.nemuelch$setAttachedEntity(first.nemuelch$getUuid());
    }

    public static void detachBoth(@Nullable Attachable first, @Nullable Attachable second) {
        if (first != null && second != null) {
            first.nemuelch$setAttachedEntity(null);
            second.nemuelch$setAttachedEntity(null);
        }
    }
}
