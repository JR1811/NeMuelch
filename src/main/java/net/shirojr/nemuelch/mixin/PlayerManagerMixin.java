package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @ModifyExpressionValue(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    private @Nullable ServerWorld replaceCustomRespawnLocationAndDimension(@Nullable ServerWorld original,
                                                                           @Local(argsOnly = true) ServerPlayerEntity player,
                                                                           @Local LocalRef<BlockPos> respawnPosition,
                                                                           @Local(ordinal = 1) LocalBooleanRef isForced) {
        ServerWorld oldServerWorld = player.getServerWorld();
        if (!oldServerWorld.getGameRules().getBoolean(NemuelchGameRules.CUSTOM_RESPAWN_LOCATIONS)) {
            return original;
        }
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(player.getServerWorld());
        Map<Identifier, RespawnLocation> locations = respawnComponent.getLocations();
        if (locations.isEmpty()) {
            return null;
        }
        RespawnLocation location = respawnComponent.chooseRandomRespawnLocation(player.getRandom(), player.getUuid());
        if (location == null) {
            return original;
        }
        respawnPosition.set(location.position());
        isForced.set(true);
        return server.getWorld(location.dimension());
    }
}
