package net.shirojr.nemuelch.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.Vec3d;

public class EntityPosing {
    public static void climb(ModelPart holdingArm, ModelPart otherArm, ModelPart head, boolean rightArmed, Vec3d targetPos, ModelPart rightLeg, ModelPart leftLeg) {
        ModelPart mainArm = rightArmed ? holdingArm : otherArm;
        ModelPart offArm = rightArmed ? otherArm : holdingArm;
        mainArm.yaw = (rightArmed ? -0.3F : 0.3F) + head.yaw;
        offArm.yaw = (rightArmed ? 0.6F : -0.6F) + head.yaw;
        mainArm.pitch = (float) (-Math.PI / 2) + head.pitch + 0.1F;
        offArm.pitch = -1.5F + head.pitch;
    }
}
