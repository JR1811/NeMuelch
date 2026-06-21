package net.shirojr.nemuelch.init;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.shirojr.nemuelch.block.custom.IronScaffoldingBlock;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.util.helper.WateringCanHelper;

public class NeMuelchProperties {
    public static final int MIN_ROTTEN_MEAT_STAGE = 0;
    public static final int MAX_ROTTEN_MEAT_STAGE = 4;
    public static final int MAX_CRYSTAL_STAGE = 4;

    public static final IntProperty DISTANCE_0_IRON_SCAFFOLDING_MAX;
    public static final BooleanProperty ROPED;
    public static final BooleanProperty ROPE_ANCHOR;
    public static final IntProperty WAND_OF_SOL_STATE;
    public static final BooleanProperty FILLED;
    public static final EnumProperty<WateringCanHelper.ItemMaterial> MATERIAL;
    public static final IntProperty QUARTER_SPLIT_PARTS;
    public static final IntProperty ROTTEN_MEAT_STAGE;
    public static final EnumProperty<CrateBlock.Type> CRATE_TYPE = EnumProperty.of("crate_type", CrateBlock.Type.class);
    public static final IntProperty CRYSTAL_STAGE = IntProperty.of("stage", 0, 4);


    static {
        DISTANCE_0_IRON_SCAFFOLDING_MAX = IntProperty.of("distance", 0, IronScaffoldingBlock.MAX_DISTANCE);
        ROPED = BooleanProperty.of("roped");
        ROPE_ANCHOR = BooleanProperty.of("rope_anchor");
        WAND_OF_SOL_STATE = IntProperty.of("wandofsol_state", 0, 2);
        FILLED = BooleanProperty.of("filled");
        MATERIAL = EnumProperty.of("material", WateringCanHelper.ItemMaterial.class);
        QUARTER_SPLIT_PARTS = IntProperty.of("quarter_split_parts", 1, 4);
        ROTTEN_MEAT_STAGE = IntProperty.of("rotten_meat_stage", MIN_ROTTEN_MEAT_STAGE, MAX_ROTTEN_MEAT_STAGE);
    }
}
