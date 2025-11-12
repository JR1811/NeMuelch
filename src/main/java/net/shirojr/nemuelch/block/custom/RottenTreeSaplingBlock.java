package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.init.NeMuelchWorldGen;

public class RottenTreeSaplingBlock extends SaplingBlock {
    public RottenTreeSaplingBlock(Settings settings) {
        super(new RottenTreeSaplingGenerator(), settings);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return super.canPlantOnTop(floor, world, pos) || floor.isOf(NeMuelchBlocks.ROTTEN_MEAT);
    }

    public static class RottenTreeSaplingGenerator extends SaplingGenerator {
        @Override
        protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random random, boolean bees) {
            return NeMuelchWorldGen.ConfiguredFeatures.ROTTEN_TREE_KEY;
        }
    }
}
