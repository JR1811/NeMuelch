package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.*;
import net.shirojr.nemuelch.entity.custom.projectile.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.entity.custom.projectile.SlimeItemEntity;

import java.util.function.Consumer;

public class NeMuelchEntities {
    public static final EntityType<OnionEntity> ONION = registerLiving("onion", SpawnGroup.MONSTER, OnionEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.7F, 0.7F))
                    .trackRangeBlocks(90).trackedUpdateRate(1)
                    .forceTrackedVelocityUpdates(true),
            OnionEntity.setAttributes()
    );

    public static final EntityType<SlimeItemEntity> SLIME_ITEM = register("slime_item", SpawnGroup.MISC, SlimeItemEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
    );

    public static final EntityType<ArkaduscaneProjectileEntity> ARKADUSCANE_PROJECTILE = register("arkaduscane_projectile", SpawnGroup.MISC, ArkaduscaneProjectileEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.3F, 0.3F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
    );

    public static final EntityType<DropPotEntity> DROP_POT = register("drop_pot", SpawnGroup.MISC, DropPotEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f))
                    .trackRangeBlocks(DropPotEntity.RENDER_DISTANCE)
    );
    public static final EntityType<PotLauncherEntity> POT_LAUNCHER = register("pot_launcher", SpawnGroup.MISC, PotLauncherEntity::new,
            entityBuilder -> entityBuilder.dimensions(EntityDimensions.fixed(PotLauncherEntity.WIDTH, PotLauncherEntity.HEIGHT))
    );


    public static final EntityType<LiftPlatformEntity> LIFT_PLATFORM = register("lift_platform", SpawnGroup.MISC, LiftPlatformEntity::new,
            builder -> builder.dimensions(EntityDimensions.fixed(4.0f, 0.3f))
    );
    public static final EntityType<LiftRopeEntity> LIFT_ROPE = register("lift_rope", SpawnGroup.MISC, LiftRopeEntity::new,
            builder -> builder.dimensions(EntityDimensions.fixed(LiftRopeEntity.SIZE, LiftRopeEntity.SIZE))
    );
    public static final EntityType<LiftRopeColliderEntity> LIFT_ROPE_COLLIDER = register("lift_rope_collider", SpawnGroup.MISC, LiftRopeColliderEntity::new,
            builder -> builder.dimensions(EntityDimensions.fixed(LiftRopeEntity.SIZE, LiftRopeEntity.SIZE))
    );
    public static final EntityType<LiftCounterWeightEntity> LIFT_COUNTER_WEIGHT = register("lift_counter_weight", SpawnGroup.MISC, LiftCounterWeightEntity::new,
            builder -> builder.dimensions(EntityDimensions.fixed(0.7f, 1.5f))
    );


    @SuppressWarnings("SameParameterValue")
    private static <T extends Entity> EntityType<T> register(String name, SpawnGroup spawnGroup, EntityType.EntityFactory<T> factory,
                                                             Consumer<FabricEntityTypeBuilder<T>> builderConsumer) {
        FabricEntityTypeBuilder<T> entityTypeBuilder = FabricEntityTypeBuilder.create(spawnGroup, factory);
        builderConsumer.accept(entityTypeBuilder);
        return Registry.register(Registry.ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name), entityTypeBuilder.build());
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends LivingEntity> EntityType<T> registerLiving(String name, SpawnGroup spawnGroup, EntityType.EntityFactory<T> factory,
                                                                         Consumer<FabricEntityTypeBuilder.Living<T>> builderConsumer,
                                                                         DefaultAttributeContainer.Builder attributesBuilder) {
        FabricEntityTypeBuilder.Living<T> entityTypeBuilder = FabricEntityTypeBuilder.createLiving();
        entityTypeBuilder.entityFactory(factory).defaultAttributes(() -> attributesBuilder).spawnGroup(spawnGroup);
        builderConsumer.accept(entityTypeBuilder);
        return Registry.register(Registry.ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, name), entityTypeBuilder.build());
    }

    public static void initialize() {
        // static initialisation
    }
}
