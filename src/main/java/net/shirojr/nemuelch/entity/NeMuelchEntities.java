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
import net.shirojr.nemuelch.entity.custom.SlimeItemEntity;

public class NeMuelchEntities {

    public static final EntityType<OnionEntity> ONION = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "onion"),
            FabricEntityTypeBuilder.<OnionEntity>create(SpawnGroup.MONSTER, OnionEntity::new)
                    .dimensions(EntityDimensions.fixed(0.7F, 0.7F)).trackRangeBlocks(90)
                    .trackedUpdateRate(1).forceTrackedVelocityUpdates(true).build());

    public static final EntityType<SlimeItemEntity> SLIME_ITEM = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "slime_item"),
            FabricEntityTypeBuilder.<SlimeItemEntity>create(SpawnGroup.MISC, SlimeItemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build());

    public static final EntityType<ArkaduscaneProjectileEntity> ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "arkaduscane_projectile"),
            FabricEntityTypeBuilder.<ArkaduscaneProjectileEntity>create(SpawnGroup.MISC, ArkaduscaneProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.3F, 0.3F))   // projectile size
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build()
    );

    public static void registerEntities() {
        EntityRendererRegistry.register(NeMuelchEntities.ONION, OnionRenderer::new);
    }
}