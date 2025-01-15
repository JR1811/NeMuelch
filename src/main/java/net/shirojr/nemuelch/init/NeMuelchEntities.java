package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.entity.custom.projectile.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.entity.custom.projectile.SlimeItemEntity;

import java.util.function.Consumer;

public class NeMuelchEntities {
    public static EntityType<OnionEntity> ONION = registerLiving("onion", SpawnGroup.MONSTER, OnionEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.7F, 0.7F))
                    .trackRangeBlocks(90).trackedUpdateRate(1)
                    .forceTrackedVelocityUpdates(true),
            OnionEntity.setAttributes()
    );

    public static EntityType<SlimeItemEntity> SLIME_ITEM = register("slime_item", SpawnGroup.MISC, SlimeItemEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
    );

    public static EntityType<ArkaduscaneProjectileEntity> ARKADUSCANE_PROJECTILE = register("arkaduscane_projectile", SpawnGroup.MISC, ArkaduscaneProjectileEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.3F, 0.3F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
    );

    public static EntityType<DropPotEntity> DROP_POT = register("drop_pot", SpawnGroup.MISC, DropPotEntity::new,
            entityBuilder -> entityBuilder
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f))
                    .trackRangeBlocks(DropPotEntity.RENDER_DISTANCE)
    );

    public static EntityType<PotLauncherEntity> POT_LAUNCHER = register("pot_launcher", SpawnGroup.MISC, PotLauncherEntity::new,
            entityBuilder -> entityBuilder.dimensions(EntityDimensions.fixed(PotLauncherEntity.WIDTH, PotLauncherEntity.HEIGHT))
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
