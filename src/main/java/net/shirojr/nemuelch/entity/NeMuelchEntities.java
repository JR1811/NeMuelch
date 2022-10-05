package net.shirojr.nemuelch.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.client.OnionRenderer;
import net.shirojr.nemuelch.entity.custom.OnionEntity;

public class NeMuelchEntities {

    /*
    public static final EntityType<OnionEntity> ONION = Registry.register(
            Registry.ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, "onion"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, OnionEntity::new)
                    .dimensions(EntityDimensions.fixed(0.85f, 0.85f)).build());
    */

    public static final EntityType<OnionEntity> ONION = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "onion"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, OnionEntity::new)
                    .dimensions(EntityDimensions.fixed(2.0F, 2.0F)).trackRangeBlocks(90)
                    .trackedUpdateRate(1).forceTrackedVelocityUpdates(true).build());

    public static void registerEntities() {

        EntityRendererRegistry.register(NeMuelchEntities.ONION, OnionRenderer::new);
        NeMuelch.LOGGER.info("registered EntityRendererRegistry in NeMuelchEntities");
    }
}
