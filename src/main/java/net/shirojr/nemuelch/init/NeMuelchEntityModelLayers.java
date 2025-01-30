package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.client.DropPotEntityModel;
import net.shirojr.nemuelch.entity.client.LiftPlatformModel;
import net.shirojr.nemuelch.entity.client.PotLauncherEntityModel;

public class NeMuelchEntityModelLayers {
    public static final EntityModelLayer DROP_POT = register("drop_pot_entity", DropPotEntityModel.getTexturedModelData());
    public static final EntityModelLayer POT_LAUNCHER = register("pot_launcher_entity", PotLauncherEntityModel.getTexturedModelData());
    public static final EntityModelLayer LIFT_PLATFORM = register("lift_platform_entity", LiftPlatformModel.getTexturedModelData());


    private static EntityModelLayer register(String name, TexturedModelData data) {
        EntityModelLayer layer = new EntityModelLayer(new Identifier(NeMuelch.MOD_ID, name), "main");
        EntityModelLayerRegistry.registerModelLayer(layer, () -> data);
        return layer;
    }

    public static void initialize() {
        // static initialisation
    }
}
