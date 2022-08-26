package net.shirojr.nemuelch.util;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.shirojr.nemuelch.entity.NeMuelchEntities;
import net.shirojr.nemuelch.entity.custom.JuggernautEntity;

public class NeMuelchRegistries {

    public static void registerContent() {

        registerAttributes();
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(NeMuelchEntities.JUGGERNAUT, JuggernautEntity.setAttributes());
    }
}
