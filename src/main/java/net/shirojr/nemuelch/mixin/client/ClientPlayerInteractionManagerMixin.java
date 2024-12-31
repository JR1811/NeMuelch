package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.revive.ReviveCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(method = "interactEntityAtLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult avoidInteraction(Entity instance, PlayerEntity player, Vec3d hitPos, Hand hand, Operation<ActionResult> original) {
        if (player.getWorld() == null) return ActionResult.PASS;
        if (ReviveCompat.shouldOpenBodyScreen(instance, player, hand)) {
            return original.call(instance, player, hitPos, hand);
        }
        return ReviveCompat.pullBody(client.world, instance, player, hand) ? ActionResult.SUCCESS : ActionResult.PASS;
    }
}
