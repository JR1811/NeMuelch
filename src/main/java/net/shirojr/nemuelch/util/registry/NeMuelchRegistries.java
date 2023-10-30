package net.shirojr.nemuelch.util.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.custom.OnionEntity;

@SuppressWarnings("DataFlowIssue")
public class NeMuelchRegistries {

    public static void register() {
        registerAttributes();
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(NeMuelchEntities.ONION, OnionEntity.setAttributes());
    }
}
