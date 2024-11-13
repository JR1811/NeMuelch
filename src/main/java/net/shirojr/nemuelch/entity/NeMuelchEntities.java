package net.shirojr.nemuelch.entity;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.client.ArkaduscaneProjectileEntityRenderer;
import net.shirojr.nemuelch.entity.client.OnionRenderer;
import net.shirojr.nemuelch.entity.client.SlimeItemEntityRenderer;
import net.shirojr.nemuelch.entity.client.TntStickItemEntityRenderer;
import net.shirojr.nemuelch.entity.custom.OnionEntity;
import net.shirojr.nemuelch.entity.custom.projectile.ArkaduscaneProjectileEntity;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;
import net.shirojr.nemuelch.entity.custom.projectile.SlimeItemEntity;
import net.shirojr.nemuelch.entity.custom.projectile.TntStickItemEntity;

public class NeMuelchEntities {

    public static final EntityType<OnionEntity> ONION = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "onion"),
            FabricEntityTypeBuilder.<OnionEntity>create(SpawnGroup.MONSTER, (type, world) -> new OnionEntity(world))
                    .dimensions(EntityDimensions.fixed(0.7F, 0.7F)).trackRangeBlocks(90)
                    .trackedUpdateRate(1).forceTrackedVelocityUpdates(true).build());

    public static final EntityType<SlimeItemEntity> SLIME_ITEM = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "slime_item"),
            FabricEntityTypeBuilder.<SlimeItemEntity>create(SpawnGroup.MISC, SlimeItemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build());

    public static final EntityType<TntStickItemEntity> TNT_STICK_ITEM = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "tnt_stick_item"),
            FabricEntityTypeBuilder.<TntStickItemEntity>create(SpawnGroup.MISC, TntStickItemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build());

    public static final EntityType<ArkaduscaneProjectileEntity> ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "arkaduscane_projectile"),
            FabricEntityTypeBuilder.<ArkaduscaneProjectileEntity>create(SpawnGroup.MISC, ArkaduscaneProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.3F, 0.3F))
                    .trackRangeBlocks(4).trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<DropPotEntity> DROP_POT = Registry.register(Registry.ENTITY_TYPE,
            new Identifier(NeMuelch.MOD_ID, "drop_pot"),
            FabricEntityTypeBuilder.<DropPotEntity>create(SpawnGroup.MISC, (type, world) -> new DropPotEntity(world))
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .trackRangeChunks(8)
                    .build()
    );

    public static void registerClient() {
        EntityRendererRegistry.register(NeMuelchEntities.ONION, OnionRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.SLIME_ITEM, SlimeItemEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.ARKADUSCANE_PROJECTILE_ENTITY_ENTITY_TYPE, ArkaduscaneProjectileEntityRenderer::new);
        EntityRendererRegistry.register(NeMuelchEntities.TNT_STICK_ITEM, TntStickItemEntityRenderer::new);
    }

    public static void register() {
        NeMuelch.devLogger("Initialize Entities");
    }
}