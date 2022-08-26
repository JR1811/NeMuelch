package net.shirojr.nemuelch.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.custom.JuggernautEntity;
import net.minecraft.util.registry.Registry;


public class NeMuelchEntities {

    public static final EntityType<JuggernautEntity> JUGGERNAUT = Registry.register(
            Registry.ENTITY_TYPE, new Identifier(NeMuelch.MOD_ID, "juggernaut"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, JuggernautEntity::new)
                    .dimensions(EntityDimensions.fixed(4f, 3f)).build());
}
