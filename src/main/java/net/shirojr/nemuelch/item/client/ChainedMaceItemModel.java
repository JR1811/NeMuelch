package net.shirojr.nemuelch.item.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

@SuppressWarnings({"FieldCanBeLocal", "unused", "SpellCheckingInspection"})
public class ChainedMaceItemModel<T extends PlayerEntity> extends SinglePartEntityModel<T> {
    private final ModelPart body;
    private final ModelPart staff;
    private final ModelPart chains;
    private final ModelPart head;
    private final ModelPart headhold;
    private final ModelPart chainhold;
    private final ModelPart chainhold1;
    private final ModelPart chainhold2;

    public ChainedMaceItemModel(ModelPart root) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.body = root.getChild("body");
        this.staff = this.body.getChild("staff");
        this.chains = this.body.getChild("chains");
        this.head = this.body.getChild("head");
        this.headhold = this.head.getChild("headhold");
        this.chainhold = this.body.getChild("chainhold");
        this.chainhold1 = this.chainhold.getChild("chainhold1");
        this.chainhold2 = this.chainhold.getChild("chainhold2");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(8.0F, 24.0F, -8.0F));

        ModelPartData staff = body.addChild("staff", ModelPartBuilder.create().uv(40, 36).cuboid(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F))
                .uv(32, 17).cuboid(-1.0F, -27.0F, -1.0F, 2.0F, 24.0F, 2.0F, new Dilation(0.0F))
                .uv(40, 17).cuboid(-1.5F, -43.0F, -1.5F, 3.0F, 16.0F, 3.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r1 = staff.addChild("cube_r1", ModelPartBuilder.create().uv(48, 49).cuboid(-2.0F, -29.6F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -30.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData chains = body.addChild("chains", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r2 = chains.addChild("cube_r2", ModelPartBuilder.create().uv(18, 55).cuboid(0.0F, -8.5034F, 1.8314F, 0.0F, 8.0F, 3.0F, new Dilation(0.0F))
                .uv(52, 17).cuboid(0.0F, -17.5034F, 2.8314F, 0.0F, 9.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, -0.1309F, 0.7854F, 0.0F));

        ModelPartData cube_r3 = chains.addChild("cube_r3", ModelPartBuilder.create().uv(42, 56).cuboid(-5.8314F, -17.5034F, 0.0F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F))
                .uv(48, 58).cuboid(-4.8314F, -8.5034F, 0.0F, 3.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, -0.1298F, 0.7769F, -0.1841F));

        ModelPartData cube_r4 = chains.addChild("cube_r4", ModelPartBuilder.create().uv(0, 55).cuboid(0.0F, -17.5034F, -5.8314F, 0.0F, 9.0F, 3.0F, new Dilation(0.0F))
                .uv(24, 55).cuboid(0.0F, -8.5034F, -4.8314F, 0.0F, 8.0F, 3.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, 0.1309F, 0.7854F, 0.0F));

        ModelPartData cube_r5 = chains.addChild("cube_r5", ModelPartBuilder.create().uv(56, 34).cuboid(2.8314F, -17.5034F, 0.0F, 3.0F, 9.0F, 0.0F, new Dilation(0.0F))
                .uv(58, 17).cuboid(1.8314F, -8.5034F, 0.0F, 3.0F, 8.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, 0.1298F, 0.7769F, 0.1841F));

        ModelPartData head = body.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData headhold = head.addChild("headhold", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r6 = headhold.addChild("cube_r6", ModelPartBuilder.create().uv(0, 17).cuboid(-1.0F, -31.0F, -8.0F, 2.0F, 15.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 38).cuboid(-8.0F, -31.0F, -1.0F, 6.0F, 15.0F, 2.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-7.4F, -24.4F, -7.4F, 15.0F, 2.0F, 15.0F, new Dilation(0.0F))
                .uv(16, 38).cuboid(2.0F, -31.0F, -1.0F, 6.0F, 15.0F, 2.0F, new Dilation(0.0F))
                .uv(16, 17).cuboid(-1.0F, -31.0F, 2.0F, 2.0F, 15.0F, 6.0F, new Dilation(0.0F))
                .uv(32, 43).cuboid(-2.0F, -18.2F, -2.0F, 4.0F, 3.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData chainhold = body.addChild("chainhold", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData chainhold1 = chainhold.addChild("chainhold1", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r7 = chainhold1.addChild("cube_r7", ModelPartBuilder.create().uv(32, 50).cuboid(-2.0F, -9.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F))
                .uv(60, 0).cuboid(-1.0F, -8.4F, 1.5F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(30, 59).cuboid(-1.0F, -8.4F, -3.5F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(54, 58).cuboid(-3.4F, -8.4F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(58, 25).cuboid(1.5F, -8.4F, -1.0F, 2.0F, 1.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -20.6F, 0.0F, 0.0F, 0.7854F, 0.0F));

        ModelPartData chainhold2 = chainhold.addChild("chainhold2", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        ModelPartData cube_r8 = chainhold2.addChild("cube_r8", ModelPartBuilder.create().uv(48, 55).cuboid(2.0F, -8.5F, -1.0F, 4.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(52, 29).cuboid(-1.0F, -8.5F, 2.0F, 2.0F, 1.0F, 4.0F, new Dilation(0.0F))
                .uv(6, 55).cuboid(-1.0F, -8.5F, -6.0F, 2.0F, 1.0F, 4.0F, new Dilation(0.0F))
                .uv(30, 56).cuboid(-6.0F, -8.5F, -1.0F, 4.0F, 1.0F, 2.0F, new Dilation(0.0F))
                .uv(48, 43).cuboid(-2.0F, -9.0F, -2.0F, 4.0F, 2.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -28.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        return TexturedModelData.of(modelData, 128, 128);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.body.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart getPart() {
        return this.body;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}
