package net.shirojr.nemuelch.util.helper;

import net.minecraft.util.math.BlockPos;

public class BlockPosHelper {
    public static final BlockPos[] CACHED_FACE_NEIGHBORS = {
            new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0), new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)
    };
    public static final BlockPos[] CACHED_EDGE_NEIGHBORS = {
            new BlockPos(1, 1, 0), new BlockPos(1, -1, 0), new BlockPos(-1, 1, 0), new BlockPos(-1, -1, 0),
            new BlockPos(1, 0, 1), new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1),
            new BlockPos(0, 1, 1), new BlockPos(0, 1, -1), new BlockPos(0, -1, 1), new BlockPos(0, -1, -1)
    };
    public static final BlockPos[] CACHED_CORNER_NEIGHBORS = {
            new BlockPos(1, 1, 1), new BlockPos(1, 1, -1), new BlockPos(1, -1, 1), new BlockPos(1, -1, -1),
            new BlockPos(-1, 1, 1), new BlockPos(-1, 1, -1), new BlockPos(-1, -1, 1), new BlockPos(-1, -1, -1)
    };

    public static final BlockPos[] ALL_NEIGHBORS_CACHED;

    static {
        ALL_NEIGHBORS_CACHED = new BlockPos[26];
        int i = 0;
        for (BlockPos pos : CACHED_FACE_NEIGHBORS) ALL_NEIGHBORS_CACHED[i++] = pos;
        for (BlockPos pos : CACHED_EDGE_NEIGHBORS) ALL_NEIGHBORS_CACHED[i++] = pos;
        for (BlockPos pos : CACHED_CORNER_NEIGHBORS) ALL_NEIGHBORS_CACHED[i++] = pos;
    }
}
