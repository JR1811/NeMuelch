package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.shirojr.nemuelch.entity.custom.projectile.DropPotEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DropPotEntityModel<T extends DropPotEntity> extends SinglePartEntityModel<T> {
    private final ModelPart base;
    private final ModelPart body;
    private final ModelPart strap;
    private final ModelPart rope;
    private final ModelPart top;
    private final List<ModelPart> parts = new ArrayList<>();

    public DropPotEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.base = root.getChild("base");
        this.body = this.base.getChild("body");
        this.strap = this.base.getChild("strap");
        this.rope = this.strap.getChild("rope");
        this.top = this.rope.getChild("top");
    }

    @Override
    public ModelPart getPart() {
        return this.base;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        ModelPartData body = base.addChild("body", ModelPartBuilder.create().uv(0, 24).cuboid(-4.0F, -16.0F, -4.0F, 8.0F, 16.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.0F, -11.0F, -7.0F, 14.0F, 10.0F, 14.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        ModelPartData strap = base.addChild("strap", ModelPartBuilder.create().uv(32, 24).cuboid(-5.0F, -14.0F, -5.0F, 10.0F, 2.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        ModelPartData rope = strap.addChild("rope", ModelPartBuilder.create().uv(32, 36).cuboid(-1.0F, -3.25F, -5.0F, 2.0F, 3.0F, 10.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -13.75F, 0.0F));
        ModelPartData top = rope.addChild("top", ModelPartBuilder.create().uv(0, 48).cuboid(-1.0F, -10.0F, -1.0F, 2.0F, 10.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -3.25F, 0.0F));

        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
