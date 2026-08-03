package net.shirojr.nemuelch.block.custom.station;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.block.entity.custom.CargoCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockPattern;
import net.shirojr.nemuelch.init.NeMuelchProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class CargoCrateBlock extends BlockWithEntity {
    private static final EnumProperty<Part> PART = NeMuelchProperties.CARGO_CRATE_PART;
    private static final NeMuelchBlockPattern CONVERSION_PATTERN = NeMuelchBlockPattern.CARGO_CRATE;

    public CargoCrateBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(PART, Part.CENTER));
    }

    public static void attemptConversion(WorldView world, BlockPos pos, LivingEntity placer, ItemStack placedWith) {
        BlockPattern.Result result = CONVERSION_PATTERN.getResult(world, pos);

    }

    public static boolean isValidCore(BlockState state) {
        return state.isIn(ConventionalBlockTags.CHESTS) && state.contains(Properties.HORIZONTAL_FACING);
    }

    @Nullable
    public static CachedBlockPosition getCore(WorldView world, BlockPos pos) {
        return CONVERSION_PATTERN.getCore(world, pos);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        if (state.get(PART) != Part.CENTER) return null;
        return new CargoCrateBlockEntity(pos, state);
    }

    public enum Part implements StringIdentifiable {
        CORNER, EDGE, FACE, CENTER;

        @Override
        public String asString() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
