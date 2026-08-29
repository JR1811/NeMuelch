package net.shirojr.nemuelch.datagen;

import net.minecraft.data.DataOutput;
import net.minecraft.registry.RegistryWrapper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.datagen.provider.SoundsFileDataProvider;
import net.shirojr.nemuelch.init.NeMuelchSounds;

import java.util.concurrent.CompletableFuture;

public class NeMuelchSoundsFileGenerator extends SoundsFileDataProvider {
    public NeMuelchSoundsFileGenerator(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, registryLookupFuture, NeMuelch.getId("sounds.existing.json"));
    }

    @Override
    protected void generate(RegistryWrapper.WrapperLookup registryLookup) {
        createSimpleEntryFromId(NeMuelchSounds.METAL_RELEASE.getId());
        createSimpleEntryFromId(NeMuelchSounds.METAL_STRIKE.getId());
    }
}
