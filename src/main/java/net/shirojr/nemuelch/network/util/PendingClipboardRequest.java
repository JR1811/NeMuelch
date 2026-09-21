package net.shirojr.nemuelch.network.util;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record PendingClipboardRequest(int duration, @Nullable List<UUID> targets) {
}
