package net.shirojr.nemuelch.compat.cca;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.shirojr.nemuelch.block.entity.custom.RottenMeatBlockEntity;
import net.shirojr.nemuelch.compat.cca.component.*;
import net.shirojr.nemuelch.compat.cca.implementation.*;
import org.jetbrains.annotations.NotNull;

public class NeMuelchComponents implements EntityComponentInitializer, ScoreboardComponentInitializer, ChunkComponentInitializer, BlockComponentInitializer, WorldComponentInitializer {
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
    public static final ComponentKey<BlightChunkTrackerComponent> BLIGHT_CHUNK_TRACKER =
            ComponentRegistry.getOrCreate(BlightChunkTrackerComponent.KEY, BlightChunkTrackerComponent.class);
    public static final ComponentKey<RottenMeatDigestionComponent> ROTTEN_MEAT_DIGESTION =
            ComponentRegistry.getOrCreate(RottenMeatDigestionComponent.KEY, RottenMeatDigestionComponent.class);
    public static final ComponentKey<MiscEntityComponent> MISC_ENTITY =
            ComponentRegistry.getOrCreate(MiscEntityComponent.KEY, MiscEntityComponent.class);
    public static final ComponentKey<DisplacementSequenceRegistryComponent> DISPLACEMENT_SEQUENCES =
            ComponentRegistry.getOrCreate(DisplacementSequenceRegistryComponent.KEY, DisplacementSequenceRegistryComponent.class);
    public static final ComponentKey<BoatDeepWaterComponent> BOAT_DEEP_WATER_SWIMMING =
            ComponentRegistry.getOrCreate(BoatDeepWaterComponent.KEY, BoatDeepWaterComponent.class);
    public static final ComponentKey<OccasionsWorldComponent> OCCASION =
            ComponentRegistry.getOrCreate(OccasionsWorldComponent.KEY, OccasionsWorldComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, ATTACHABLE, AttachableComponentImpl::new);
        registry.registerFor(LivingEntity.class, MONSTER, GeneralMonsterComponentImpl::new);
        registry.registerForPlayers(ACT_COMMAND, ActCommandComponentImpl::new, ActCommandComponentImpl::onRespawn);
        registry.registerFor(LivingEntity.class, BLIGHT_ENTITY, BlightEntityComponentImpl::new);
        registry.registerFor(LivingEntity.class, MISC_ENTITY, MiscEntityComponent::new);
        registry.registerFor(BoatEntity.class, BOAT_DEEP_WATER_SWIMMING, BoatDeepWaterComponent::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(RESPAWN_LOCATIONS, RespawnLocationsComponentImpl::new);
        registry.registerScoreboardComponent(BLIGHT_CHUNK_TRACKER, BlightChunkTrackerComponent::new);
        registry.registerScoreboardComponent(DISPLACEMENT_SEQUENCES, (scoreboard, minecraftServer) -> new DisplacementSequenceRegistryComponent(scoreboard));
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(BLIGHT_CHUNK, BlightChunkComponentImpl::new);
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(RottenMeatBlockEntity.class, ROTTEN_MEAT_DIGESTION, RottenMeatDigestionComponent::new);
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(OCCASION, OccasionsWorldComponent::new);
    }
}
