package net.shirojr.nemuelch.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.entity.custom.PotLauncherEntity;
import net.shirojr.nemuelch.network.NeMuelchC2SPacketHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "updateMouse",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"),
            cancellable = true)
    private void nemuelch$stuckEffectMovementMultiplier(CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        if (clientPlayer == null || clientPlayer.isSpectator()) return;
        for (StatusEffect effect : NeMuelchEffects.STUCK_EFFECTS) {
            if (!clientPlayer.hasStatusEffect(effect)) continue;
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void readMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        GameOptions options = client.options;
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) return;
        boolean mouseScrolled = options.discreteMouseScroll;
        double delta = (mouseScrolled ? Math.signum(horizontal) : vertical) * options.mouseWheelSensitivity;


        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector().normalize().multiply(5.0);
        Vec3d end = start.add(direction);

        Box searchBox = new Box(start, end).expand(1.0);
        List<Entity> entitiesInRange = world.getOtherEntities(player, searchBox,
                entity -> entity instanceof PotLauncherEntity && entity.isCollidable());

        Map.Entry<PotLauncherEntity.InteractionHitBox, Box> closestInteraction = null;
        PotLauncherEntity selectedEntity = null;
        double closestRange = Double.MAX_VALUE;
        for (Entity entity : entitiesInRange) {
            if (!(entity instanceof PotLauncherEntity potLauncherEntity)) continue;
            for (var entry : potLauncherEntity.getInteractionBoxes().entrySet()) {
                Box worldSpaceBox = entry.getValue().offset(potLauncherEntity.getPos());
                if (worldSpaceBox.raycast(start, end).isPresent()) {
                    double sqDistance = worldSpaceBox.raycast(start, end).get().squaredDistanceTo(start);
                    if (closestInteraction == null || closestRange > sqDistance) {
                        closestInteraction = entry;
                        closestRange = sqDistance;
                        selectedEntity = potLauncherEntity;
                    }
                }
            }
        }
        if (closestInteraction == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(selectedEntity.getId());
        buf.writeDouble(delta);
        buf.writeString(closestInteraction.getKey().asString());

        ClientPlayNetworking.send(NeMuelchC2SPacketHandler.MOUSE_SCROLLED_CHANNEL, buf);
        ci.cancel();
    }
}
