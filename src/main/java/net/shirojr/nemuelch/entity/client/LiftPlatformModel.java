package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.shirojr.nemuelch.entity.custom.LiftPlatformEntity;

public class LiftPlatformModel<T extends LiftPlatformEntity> extends SinglePartEntityModel<T> {
    private final ModelPart base, palette, planks, connectorPlanks, straps, diagonal, top, holder;

    public LiftPlatformModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.base = root.getChild("base");
        this.palette = this.base.getChild("palette");
        this.connectorPlanks = this.palette.getChild("connector_planks");
        this.planks = this.palette.getChild("planks");
        this.straps = this.base.getChild("straps");
        this.diagonal = this.straps.getChild("diagonal");
        this.top = this.straps.getChild("top");
        this.holder = this.base.getChild("holder");
    }

    @SuppressWarnings("unused")
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData palette = base.addChild("palette", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData connectorPlanks = palette.addChild("connector_planks", ModelPartBuilder.create().uv(0, 0).cuboid(20.0F, 0.0F, -22.0F, 6.0F, 2.0F, 44.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, 0.0F, -22.0F, 6.0F, 2.0F, 44.0F, new Dilation(0.0F)), ModelTransform.pivot(-12.0F, 0.0F, 0.0F));

        ModelPartData planks = palette.addChild("planks", ModelPartBuilder.create().uv(3, 49).cuboid(-16.0F, 0.0F, 39.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(3, 49).cuboid(-16.0F, 0.0F, 46.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(3, 49).mirrored().cuboid(-16.0F, 0.0F, 32.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false)
                .uv(3, 49).mirrored().cuboid(-16.0F, 0.0F, 25.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false)
                .uv(3, 49).cuboid(-16.0F, 0.0F, 4.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F))
                .uv(3, 49).mirrored().cuboid(-16.0F, 0.0F, 11.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F)).mirrored(false)
                .uv(3, 49).cuboid(-16.0F, 0.0F, 18.0F, 32.0F, 2.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 1.0F, -28.0F));

        ModelPartData straps = base.addChild("straps", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -1.75F, 0.0F));

        ModelPartData diagonal = straps.addChild("diagonal", ModelPartBuilder.create(), ModelTransform.pivot(-11.0F, 4.0F, 21.0F));

        ModelPartData cube_r1 = diagonal.addChild("cube_r1", ModelPartBuilder.create().uv(2, 0).mirrored().cuboid(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 2.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(23.5F, 21.75F, -22.0F, -2.3562F, 0.0F, 3.1416F));

        ModelPartData cube_r2 = diagonal.addChild("cube_r2", ModelPartBuilder.create().uv(2, 0).cuboid(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, 21.75F, -22.0F, -2.3562F, 0.0F, -3.1416F));

        ModelPartData cube_r3 = diagonal.addChild("cube_r3", ModelPartBuilder.create().uv(2, 0).cuboid(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-1.5F, 21.75F, -20.0F, 0.7854F, 0.0F, 0.0F));

        ModelPartData cube_r4 = diagonal.addChild("cube_r4", ModelPartBuilder.create().uv(2, 0).mirrored().cuboid(0.0F, -30.0F, -1.0F, 0.0F, 30.0F, 2.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(23.5F, 21.75F, -20.0F, 0.7854F, 0.0F, 0.0F));

        ModelPartData top = straps.addChild("top", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r5 = top.addChild("cube_r5", ModelPartBuilder.create().uv(15, 20).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(1.0F, 26.75F, 0.0F, 0.0F, -0.7854F, 1.5708F));

        ModelPartData cube_r6 = top.addChild("cube_r6", ModelPartBuilder.create().uv(15, 20).mirrored().cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-1.0F, 26.75F, 0.0F, 0.0F, 0.7854F, -1.5708F));

        ModelPartData cube_r7 = top.addChild("cube_r7", ModelPartBuilder.create().uv(15, 20).mirrored().cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.of(-13.0F, 26.75F, 0.0F, 0.0F, 0.7854F, -1.5708F));

        ModelPartData cube_r8 = top.addChild("cube_r8", ModelPartBuilder.create().uv(15, 20).cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(13.0F, 26.75F, 0.0F, 0.0F, -0.7854F, 1.5708F));

        ModelPartData cube_r9 = top.addChild("cube_r9", ModelPartBuilder.create().uv(3, 63).cuboid(-17.0F, -0.9571F, -0.9571F, 34.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 26.7071F, -0.0429F, -0.7854F, 0.0F, 0.0F));

        ModelPartData holder = base.addChild("holder", ModelPartBuilder.create().uv(16, 16).cuboid(11.5F, 3.0F, -23.0F, 2.0F, 1.0F, 3.0F, new Dilation(0.0F))
                .uv(16, 16).cuboid(-13.5F, 3.0F, -23.0F, 2.0F, 1.0F, 3.0F, new Dilation(0.0F))
                .uv(16, 16).cuboid(-13.5F, 3.0F, 20.0F, 2.0F, 1.0F, 3.0F, new Dilation(0.0F))
                .uv(16, 16).cuboid(11.5F, 3.0F, 20.0F, 2.0F, 1.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public ModelPart getPart() {
        return this.base;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
