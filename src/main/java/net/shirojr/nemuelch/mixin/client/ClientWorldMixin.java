package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @ModifyExpressionValue(method = "getBlockParticle", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean addInvisibleBlockBillboardSpriteRendering(boolean original, @Local Item itemInHand) {
        return original || itemInHand.equals(NeMuelchItems.ADVANCED_FOG);
    }

    @WrapMethod(method = "getSkyBrightness")
    private float adjustSkyBrightness(float tickDelta, Operation<Float> original) {
        float oldValue = original.call(tickDelta);
        OccasionsWorldComponent component = OccasionsWorldComponent.get((ClientWorld) (Object) this);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            OptionalDouble skyBrightness = entry.getType().getSkyBrightness(oldValue, tickDelta);
            if (skyBrightness.isPresent()) return (float) skyBrightness.getAsDouble();
        }
        return oldValue;
    }

    @WrapMethod(method = "getSkyColor")
    private Vec3d adjustSkyColor(Vec3d cameraPos, float tickDelta, Operation<Vec3d> original) {
        OccasionsWorldComponent component = OccasionsWorldComponent.get((ClientWorld) (Object) this);
        Vec3d oldColor = original.call(cameraPos, tickDelta);
        for (OccasionEntry entry : component.getUnsyncedActiveOccasions()) {
            Optional<Vec3d> skyColor = entry.getType().getSkyColor(oldColor);
            if (skyColor.isPresent()) return skyColor.get();
        }
        return oldColor;
    }
}
