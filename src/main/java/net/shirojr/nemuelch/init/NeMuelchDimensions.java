package net.shirojr.nemuelch.init;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.util.data.RegistryKeyHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

@SuppressWarnings("unused")
public interface NeMuelchDimensions {
    List<RegistryKeyHolder<DimensionType>> ALL = new ArrayList<>();

    RegistryKeyHolder<DimensionType> BACKYARD = register(
            "backyard",
            new DimensionType(
                    OptionalLong.of(18000), false, false, false,
                    true, 1.0, true, true, -128, 560, 560, BlockTags.INFINIBURN_OVERWORLD,
                    DimensionTypes.THE_NETHER_ID, 0.0f,
                    new DimensionType.MonsterSettings(true, false, ConstantIntProvider.create(0), 0)
            )
    );

    @SuppressWarnings("SameParameterValue")
    private static RegistryKeyHolder<DimensionType> register(String name, DimensionType entry) {
        RegistryKeyHolder<DimensionType> holder = new RegistryKeyHolder<>(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, NeMuelch.getId(name)), entry);
        ALL.add(holder);
        return holder;
    }

    static void initialize() {
        // static initialisation
    }
}
