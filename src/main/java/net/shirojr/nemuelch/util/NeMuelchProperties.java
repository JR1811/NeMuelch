package net.shirojr.nemuelch.util;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.shirojr.nemuelch.block.custom.IronScaffoldingBlock;
import org.lwjgl.system.CallbackI;

public class NeMuelchProperties {
    public static final IntProperty DISTANCE_0_IRON_SCAFFOLDING_MAX;
    public static final BooleanProperty ROPED;
    public static final BooleanProperty ROPE_END;
    public static final BooleanProperty ROPE_ANCHOR;

    static {
        DISTANCE_0_IRON_SCAFFOLDING_MAX = IntProperty.of("distance", 0, IronScaffoldingBlock.MAX_DISTANCE);
        ROPED = BooleanProperty.of("roped");
        ROPE_END = BooleanProperty.of("rope_end");
        ROPE_ANCHOR = BooleanProperty.of("rope_anchor");
    }
}
