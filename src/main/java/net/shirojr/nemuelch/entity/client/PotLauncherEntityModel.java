package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class PotLauncherEntityModel<T extends PotLauncherEntity> extends SinglePartEntityModel<T> {
    private final ModelPart base;
    private final ModelPart legs;
    private final ModelPart crank;
    private final ModelPart puller;
    private final ModelPart rotator;
    private final ModelPart slider;
    private final ModelPart strap;
    private final ModelPart back;
    private final List<ModelPart> parts = new ArrayList<>();

    public PotLauncherEntityModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.base = root.getChild("base");
        this.legs = this.base.getChild("legs");
        this.crank = this.legs.getChild("crank");
        this.puller = this.legs.getChild("puller");
        this.rotator = this.base.getChild("rotator");
        this.slider = this.rotator.getChild("slider");
        this.strap = this.rotator.getChild("strap");
        this.back = this.strap.getChild("back");
    }

    @Override
    public ModelPart getPart() {
        return this.base;
    }

    public ModelPart getSliderPart() {
        return this.slider;
    }

    public ModelPart getStrapPart() {
        return this.strap;
    }

    public ModelPart getBackStrapPart() {
        return this.back;
    }

    @SuppressWarnings("unused")
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData base = modelPartData.addChild("base", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData legs = base.addChild("legs", ModelPartBuilder.create().uv(0, 44).cuboid(-9.0F, -1.25F, -5.0F, 18.0F, 1.0F, 4.0F, new Dilation(0.0F))
                .uv(0, 26).cuboid(5.0F, -2.0F, -6.0F, 3.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(38, 26).cuboid(-8.0F, -2.0F, -6.0F, 3.0F, 2.0F, 16.0F, new Dilation(0.0F))
                .uv(0, 65).cuboid(-7.5F, -22.0F, -4.0F, 2.0F, 20.0F, 2.0F, new Dilation(0.0F))
                .uv(8, 65).cuboid(5.5F, -22.0F, -4.0F, 2.0F, 20.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, -2.0F));

        ModelPartData cube_r1 = legs.addChild("cube_r1", ModelPartBuilder.create().uv(58, 73).cuboid(0.0F, -16.0F, -1.0F, 1.0F, 17.0F, 2.0F, new Dilation(0.0F))
                .uv(52, 73).cuboid(14.5F, -16.0F, -1.0F, 1.0F, 17.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-7.75F, -2.0F, 4.0F, 0.4451F, 0.0F, 0.0F));

        ModelPartData crank = legs.addChild("crank", ModelPartBuilder.create().uv(9, 4).cuboid(0.25F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(3, 6).cuboid(2.25F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(7.25F, -20.0F, -3.0F));

        ModelPartData puller = legs.addChild("puller", ModelPartBuilder.create().uv(1, 1).cuboid(-10.0F, -14.0F, -3.5F, 1.0F, 6.0F, 1.0F, new Dilation(0.0F))
                .uv(9, 1).cuboid(-9.0F, -13.0F, -3.5F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F))
                .uv(9, 1).cuboid(-9.0F, -10.0F, -3.5F, 2.0F, 1.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData rotator = base.addChild("rotator", ModelPartBuilder.create().uv(56, 54).cuboid(4.0F, -2.0F, -2.0F, 2.0F, 9.0F, 10.0F, new Dilation(0.0F))
                .uv(58, 0).cuboid(-6.0F, -2.0F, -2.0F, 2.0F, 9.0F, 10.0F, new Dilation(0.0F))
                .uv(44, 44).cuboid(-5.0F, 6.0F, -1.0F, 10.0F, 2.0F, 8.0F, new Dilation(0.0F))
                .uv(64, 73).cuboid(5.5F, -1.0F, 7.0F, 2.0F, 7.0F, 2.0F, new Dilation(0.0F))
                .uv(16, 70).cuboid(4.5F, 0.75F, 0.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(34, 70).cuboid(-6.5F, 0.75F, 0.0F, 2.0F, 2.0F, 7.0F, new Dilation(0.0F))
                .uv(72, 73).cuboid(-7.5F, -1.0F, 7.0F, 2.0F, 7.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -21.0F, -5.0F));

        ModelPartData slider = rotator.addChild("slider", ModelPartBuilder.create().uv(28, 49).cuboid(-3.0F, -2.0F, 14.0F, 6.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-2.0F, -1.0F, -11.0F, 4.0F, 1.0F, 25.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 7.5F, 9.0F));

        ModelPartData strap = rotator.addChild("strap", ModelPartBuilder.create().uv(0, 49).cuboid(6.0F, 1.0F, 9.0F, 0.0F, 2.0F, 14.0F, new Dilation(0.0F))
                .uv(0, 47).cuboid(-6.0F, 1.0F, 9.0F, 0.0F, 2.0F, 14.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData back = strap.addChild("back", ModelPartBuilder.create().uv(64, 23).cuboid(-6.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new Dilation(0.0F))
                .uv(58, 19).cuboid(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 0.0F, new Dilation(0.0F))
                .uv(58, 23).cuboid(3.0F, -1.0F, 0.0F, 3.0F, 2.0F, 0.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 2.0F, 23.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        setAngles(entity, 0.0f);
    }

    public void setAngles(T entity, float tickDelta) {
        EulerAngle angles = entity.getAngles();
        this.base.yaw = (float) Math.toRadians(angles.getYaw() + 180);
        this.rotator.roll = (float) Math.toRadians(angles.getRoll());
        this.rotator.pitch = (float) Math.toRadians(angles.getPitch());
        this.crank.pitch = (float) Math.toRadians(angles.getPitch());

        float interpolatedTicks = entity.isActivated() ? entity.getActivationTicks() + tickDelta : 0;
        float normalizedPosition = interpolatedTicks / PotLauncherEntity.ACTIVATION_DURATION;
        this.slider.pivotZ = MathHelper.lerp(normalizedPosition * normalizedPosition * normalizedPosition, 6.0f, -8.0f);       // -5 = fully shot
    }

    @Override
    public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
        super.animateModel(entity, limbAngle, limbDistance, tickDelta);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.base.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
