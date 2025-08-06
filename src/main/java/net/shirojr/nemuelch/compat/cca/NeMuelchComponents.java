package net.shirojr.nemuelch.compat.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.implementation.RespawnLocationsComponentImpl;

public class NeMuelchComponents implements EntityComponentInitializer, ScoreboardComponentInitializer {
    public static final ComponentKey<RespawnLocationsComponent> RESPAWN_LOCATIONS =
            ComponentRegistry.getOrCreate(RespawnLocationsComponent.KEY, RespawnLocationsComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(RESPAWN_LOCATIONS, RespawnLocationsComponentImpl::new);
    }
}
