package net.shirojr.nemuelch.init;

import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.shirojr.nemuelch.block.custom.station.CargoCrateBlock;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public enum NeMuelchBlockPattern {
    CARGO_CRATE(
            BlockPatternBuilder.start()
                    .aisle("www", "www", "www")
                    .aisle("www", "wcw", "www")
                    .aisle("www", "www", "www")
                    .where('w', CachedBlockPosition.matchesBlockState(CargoCrateBlock::isValidWallState))
                    .where('c', CachedBlockPosition.matchesBlockState(CargoCrateBlock::isValidCoreState))
                    .build(),
            new BlockPos(1, 1, 1)
    );

    private final BlockPattern pattern;
    @Nullable
    private final BlockPos relativeCorePos;

    NeMuelchBlockPattern(BlockPattern pattern, @Nullable BlockPos relativeCorePos) {
        this.pattern = pattern;
        this.relativeCorePos = relativeCorePos;
    }

    public BlockPattern getPattern() {
        return pattern;
    }

    @Nullable
    public BlockPattern.Result getResult(WorldView world, BlockPos pos) {
        return this.getPattern().searchAround(world, pos);
    }

    @Nullable
    public CachedBlockPosition getCore(WorldView world, BlockPos posInStructure) {
        if (this.relativeCorePos == null) return null;
        BlockPattern.Result result = getResult(world, posInStructure);
        if (result == null) return null;
        return result.translate(this.relativeCorePos.getX(), this.relativeCorePos.getY(), this.relativeCorePos.getZ());
    }

    @Nullable
    public Collection<CachedBlockPosition> getEntries(WorldView world, BlockPos posInStructure) {
        return this.getEntries(world, posInStructure, cachedBlockPosition -> cachedBlockPosition.getBlockState().isAir());
    }

    @Nullable
    public Collection<CachedBlockPosition> getEntries(WorldView world, BlockPos posInStructure, Predicate<CachedBlockPosition> exclude) {
        if (this.relativeCorePos == null) return null;
        BlockPattern.Result patternResult = getResult(world, posInStructure);
        if (patternResult == null) return null;
        List<CachedBlockPosition> output = new ArrayList<>();
        for (int x = 0; x < pattern.getWidth(); x++) {
            for (int y = 0; y < pattern.getHeight(); y++) {
                for (int z = 0; z < pattern.getDepth(); z++) {
                    CachedBlockPosition entry = patternResult.translate(x, y, z);
                    if (exclude.test(entry)) continue;
                    output.add(entry);
                }
            }
        }
        return output;
    }
}
