package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @ModifyVariable(
            method = "method_8361",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
            )
    )
    private static byte addRelaxedCreativeReducedDebugInfoCheck(byte original, @Local ServerPlayerEntity player) {
        if (!NeMuelchConfigInit.CONFIG.disableReducedDebugInfoForOperators) return original;
        if (original == EntityStatuses.USE_FULL_DEBUG_INFO) return original;
        return player.hasPermissionLevel(2) ? EntityStatuses.USE_FULL_DEBUG_INFO : EntityStatuses.USE_REDUCED_DEBUG_INFO;
    }
}
