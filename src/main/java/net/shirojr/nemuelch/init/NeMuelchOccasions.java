package net.shirojr.nemuelch.init;

import net.minecraft.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.occasion.type.AzurePhase;
import net.shirojr.nemuelch.occasion.type.CrimsonPhase;
import net.shirojr.nemuelch.occasion.type.MaroonPhase;
import net.shirojr.nemuelch.occasion.util.OccasionType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface NeMuelchOccasions {
    List<OccasionType> ALL_OCCASION_TYPES = new ArrayList<>();

    CrimsonPhase CRIMSON_PHASE = register("crimson_phase", new CrimsonPhase(1000, 100));
    AzurePhase AZURE_PHASE = register("azure_phase", new AzurePhase(1000, 100));
    MaroonPhase MAROON_PHASE = register("maroon_phase", new MaroonPhase(1000));


    @SuppressWarnings("SameParameterValue")
    private static <T extends OccasionType> T register(String name, T entry) {
        T registeredEntry = Registry.register(NeMuelchCustomRegistries.OCCASIONS, NeMuelch.getId(name), entry);
        ALL_OCCASION_TYPES.add(registeredEntry);
        return registeredEntry;
    }

    static void initialize() {
        // static initialisation
    }
}
