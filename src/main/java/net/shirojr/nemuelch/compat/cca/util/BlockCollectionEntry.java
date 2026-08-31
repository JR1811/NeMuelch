package net.shirojr.nemuelch.compat.cca.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public record BlockCollectionEntry(long creationTime, ObjectArrayList<BlockSnapshot> blocks) {
    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }

    public BlockCollectionEntry copyWithTime(long newTime) {
        return new BlockCollectionEntry(newTime, new ObjectArrayList<>(blocks));
    }
}
