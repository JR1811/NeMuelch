package net.shirojr.nemuelch.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.AcaciaFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.shirojr.nemuelch.init.NeMuelchProperties;

public class RottenMeatFoliagePlacer extends AcaciaFoliagePlacer {
    public RottenMeatFoliagePlacer(IntProvider intProvider, IntProvider intProvider2) {
        super(intProvider, intProvider2);
    }

    @Override
    protected void generateSquare(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos centerPos, int radius, int y, boolean giantTrunk) {
        int i = giantTrunk ? 1 : 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int j = -radius; j <= radius + i; j++) {
            for (int k = -radius; k <= radius + i; k++) {
                if (!this.isPositionInvalid(random, j, y, k, radius, giantTrunk)) {
                    mutable.set(centerPos, j, y, k);
                    placeStagedFoliageBlock(world, placer, random, config, mutable);
                }
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static boolean placeStagedFoliageBlock(TestableWorld world, FoliagePlacer.BlockPlacer placer, Random random, TreeFeatureConfig config, BlockPos pos) {
        if (!TreeFeature.canReplace(world, pos)) {
            return false;
        } else {
            BlockState blockState = config.foliageProvider.get(random, pos);
            if (blockState.contains(Properties.WATERLOGGED)) {
                blockState = blockState.with(Properties.WATERLOGGED, world.testFluidState(pos, fluidState -> fluidState.isEqualAndStill(Fluids.WATER)));
            }
            if (blockState.contains(NeMuelchProperties.ROTTEN_MEAT_STAGE)) {
                blockState = blockState.with(NeMuelchProperties.ROTTEN_MEAT_STAGE,
                        random.nextBetween(NeMuelchProperties.MIN_ROTTEN_MEAT_STAGE, NeMuelchProperties.MAX_ROTTEN_MEAT_STAGE)
                );
            }

            placer.placeBlock(pos, blockState);
            return true;
        }
    }
}
