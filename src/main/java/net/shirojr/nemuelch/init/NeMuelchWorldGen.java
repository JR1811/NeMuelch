package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.intprovider.WeightedListIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.AcaciaFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.CherryTrunkPlacer;
import net.shirojr.nemuelch.NeMuelch;

public interface NeMuelchWorldGen {
    @SuppressWarnings("SameParameterValue")
    interface ConfiguredFeatures {
        RegistryKey<ConfiguredFeature<?, ?>> ROTTEN_TREE_KEY = registerKey("rotten_tree");

        private static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
            return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, NeMuelch.getId(name));
        }

        private static <FC extends FeatureConfig, F extends Feature<FC>> void register(
                Registerable<ConfiguredFeature<?, ?>> context, RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
            context.register(key, new ConfiguredFeature<>(feature, configuration));
        }

        static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
            register(context, ROTTEN_TREE_KEY, Feature.TREE,
                    new TreeFeatureConfig.Builder(
                            BlockStateProvider.of(NeMuelchBlocks.ROTTEN_TREE_LOG),
                            new CherryTrunkPlacer(6, 3, 2,
                                    new WeightedListIntProvider(
                                            DataPool.<IntProvider>builder()
                                                    .add(ConstantIntProvider.create(2), 2)
                                                    .add(ConstantIntProvider.create(1), 1)
                                                    .build()
                                    ),
                                    UniformIntProvider.create(2, 3),
                                    UniformIntProvider.create(-3, -1),
                                    UniformIntProvider.create(0, 1)
                            ),

                            BlockStateProvider.of(NeMuelchBlocks.ROTTEN_MEAT),
                            new AcaciaFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(1)),

                            new TwoLayersFeatureSize(1, 0, 2)
                    ).build()
            );
        }

        static void initialize() {
            // static initialisation
        }
    }

    static void initialize() {
        ConfiguredFeatures.initialize();
    }
}
