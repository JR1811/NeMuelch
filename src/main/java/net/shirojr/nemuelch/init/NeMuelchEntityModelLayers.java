package net.shirojr.nemuelch.init;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.entity.client.DropPotEntityModel;
import net.shirojr.nemuelch.entity.client.DummyCloseQuarterEntityModel;
import net.shirojr.nemuelch.entity.client.LiftPlatformModel;
import net.shirojr.nemuelch.entity.client.PotLauncherEntityModel;
import net.shirojr.nemuelch.item.client.ChainedMaceItemModel;

public interface NeMuelchEntityModelLayers {
    EntityModelLayer DROP_POT = register("drop_pot_entity", DropPotEntityModel.getTexturedModelData());
    EntityModelLayer DUMMY_CQC = register("dummy_cqc", DummyCloseQuarterEntityModel.getTexturedModelData());
    EntityModelLayer POT_LAUNCHER = register("pot_launcher_entity", PotLauncherEntityModel.getTexturedModelData());
    EntityModelLayer LIFT_PLATFORM = register("lift_platform_entity", LiftPlatformModel.getTexturedModelData());
    EntityModelLayer CHAINED_MACE = register("chained_mace", ChainedMaceItemModel.getTexturedModelData());


    private static EntityModelLayer register(String name, TexturedModelData data) {
        EntityModelLayer layer = new EntityModelLayer(new Identifier(NeMuelch.MOD_ID, name), "main");
        EntityModelLayerRegistry.registerModelLayer(layer, () -> data);
        return layer;
    }

    static void initialize() {
        // static initialisation
    }
}
