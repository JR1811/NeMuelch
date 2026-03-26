package net.shirojr.nemuelch.entity.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.network.packet.DummyHitS2CPacket;

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

        ModelPartData top = base.addChild("top", ModelPartBuilder.create().uv(0, 21).cuboid(-3.0F, -29.0F, -3.0F, 6.0F, 20.0F, 6.0F, new Dilation(0.0F))
                .uv(0, 13).cuboid(-10.0F, -14.0F, -1.0F, 20.0F, 2.0F, 2.0F, new Dilation(0.0F))
                .uv(24, 30).cuboid(-2.0F, -9.0F, -2.0F, 4.0F, 11.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, -3.0F, 0.0F));

        ModelPartData cube_r1 = top.addChild("cube_r1", ModelPartBuilder.create().uv(0, 17).cuboid(-10.0F, -1.0F, -1.0F, 20.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -26.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        this.top.pitch = 0f;
        this.top.roll = 0f;

        if (entity.getClientHitAge() == -1) return;
        DummyHitS2CPacket hitData = entity.getClientHitData();
        if (hitData == null) return;
        float timeSinceHit = animationProgress - entity.getClientHitAge();
        float maxPossibleDamage = 50;
        float normalizedDamage = MathHelper.clamp(hitData.damage(), 0, maxPossibleDamage) / maxPossibleDamage;
        if (timeSinceHit < 0 || timeSinceHit > DummyCloseQuarterEntity.BASE_ROCKING_DURATION) {
            entity.resetClientHitData();
            return;
        }

        float decayRate = 0.1f;
        float oscillationSpeed = 0.8f;
        float maxAngleInRad = (float) Math.toRadians(70);
        float decay = (float) Math.exp(-timeSinceHit * decayRate);
        float rock = MathHelper.sin(timeSinceHit * oscillationSpeed) * (maxAngleInRad * normalizedDamage) * decay;


        float angle = hitData.angleInRad() + (float) (Math.PI / 2);
        this.top.pitch = MathHelper.cos(angle) * rock;
        this.top.roll = -MathHelper.sin(angle) * rock;
    }
}
