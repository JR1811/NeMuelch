package net.shirojr.nemuelch.util.helper;

import net.shirojr.nemuelch.compat.cca.component.AttachableComponent;
import org.jetbrains.annotations.Nullable;

public class AttachableHelper {
    public static void attachBoth(AttachableComponent first, AttachableComponent second) {
        first.setAttachedEntity(second.getSelf());
        second.setAttachedEntity(first.getSelf());
    }

    public static void detachBoth(@Nullable AttachableComponent first, @Nullable AttachableComponent second) {
        if (first != null) {
            first.setAttachedEntity(null);
        }
        if (second != null) {
            second.setAttachedEntity(null);
        }
    }
}
