package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Unique
    private static final Identifier TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/misc/slime_overlay.png");

    @Shadow
    protected abstract void renderOverlay(Identifier texture, float opacity);

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I", shift = At.Shift.BEFORE))
    private void nemuelch$renderAdditionalOverlays(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.player == null) return;
        ClientPlayerEntity player = client.player;

        if (player.hasStatusEffect(NeMuelchEffects.SLIMED)) {
            renderOverlay(TEXTURE, 1.0f);
        }
    }
}
