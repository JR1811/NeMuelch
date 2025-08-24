package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DummyCloseQuarterEntityModel<T extends DummyCloseQuarterEntity> extends SinglePartEntityModel<T> {
    private final ModelPart base;
    private final ModelPart top;

    public DummyCloseQuarterEntityModel(ModelPart root) {
        this.base = root.getChild("base");
        this.top = this.base.getChild("top");
    }

    @Override
    public ModelPart getPart() {
        return this.base;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F, new Dilation(0.0F))
                .uv(24, 21).cuboid(-3.0F, -4.0F, -3.0F, 6.0F, 3.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData top = base.addChild("top", ModelPartBuilder.create().uv(0, 21).cuboid(-3.0F, -19.0F, -3.0F, 6.0F, 13.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 13).cuboid(-10.0F, -10.0F, -1.0F, 20.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(24, 30).cuboid(-2.0F, -6.0F, -2.0F, 4.0F, 8.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -3.0F, 0.0F));

        ModelPartData cube_r1 = top.addChild("cube_r1", ModelPartBuilder.create().uv(0, 17).cuboid(-10.0F, -1.0F, -1.0F, 20.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -16.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}
