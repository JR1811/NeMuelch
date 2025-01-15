package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.revive.ReviveCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$1")
public class ServerPlayNetworkHandlerMixin {
    @WrapOperation(method = "method_33898", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private static ActionResult avoidInteractAtCall(Entity instance, PlayerEntity player, Vec3d hitPos, Hand hand, Operation<ActionResult> original) {
        if (ReviveCompat.shouldOpenBodyScreen(instance, player, hand)) {
            return original.call(instance, player, hitPos, hand);
        }
        ReviveCompat.pullBody(instance.getWorld(), instance, player, hand);
        return ActionResult.SUCCESS;
    }
}
