package net.shirojr.nemuelch.compat.cca;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.shirojr.nemuelch.compat.cca.component.*;
import net.shirojr.nemuelch.compat.cca.implementation.*;

public class NeMuelchComponents implements EntityComponentInitializer, ScoreboardComponentInitializer, ChunkComponentInitializer {
    public static final ComponentKey<RespawnLocationsComponent> RESPAWN_LOCATIONS =
            ComponentRegistry.getOrCreate(RespawnLocationsComponent.KEY, RespawnLocationsComponent.class);
    public static final ComponentKey<AttachableComponent> ATTACHABLE =
            ComponentRegistry.getOrCreate(AttachableComponent.KEY, AttachableComponent.class);
    public static final ComponentKey<GeneralMonsterComponent> MONSTER =
            ComponentRegistry.getOrCreate(GeneralMonsterComponent.KEY, GeneralMonsterComponent.class);
    public static final ComponentKey<ActCommandComponent> ACT_COMMAND =
            ComponentRegistry.getOrCreate(ActCommandComponent.KEY, ActCommandComponent.class);
    public static final ComponentKey<BlightChunkComponent> BLIGHT_CHUNK =
            ComponentRegistry.getOrCreate(BlightChunkComponent.KEY, BlightChunkComponent.class);
    public static final ComponentKey<BlightEntityComponent> BLIGHT_ENTITY =
            ComponentRegistry.getOrCreate(BlightEntityComponent.KEY, BlightEntityComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, ATTACHABLE, AttachableComponentImpl::new);
        registry.registerFor(LivingEntity.class, MONSTER, GeneralMonsterComponentImpl::new);
        registry.registerForPlayers(ACT_COMMAND, ActCommandComponentImpl::new, ActCommandComponentImpl::onRespawn);
        registry.registerFor(LivingEntity.class, BLIGHT_ENTITY, BlightEntityComponentImpl::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(RESPAWN_LOCATIONS, RespawnLocationsComponentImpl::new);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(BLIGHT_CHUNK, BlightChunkComponentImpl::new);
    }
}
