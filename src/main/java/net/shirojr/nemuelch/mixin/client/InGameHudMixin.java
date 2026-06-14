package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
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
    private static final Identifier SLIME_OVERLAY_TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/misc/slime_overlay.png");
    @Unique
    private static final Identifier ACID_HEART_TEXTURES = NeMuelch.getId("textures/gui/heart_acid.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void nemuelch$renderAdditionalOverlays(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (client.player == null) return;
        ClientPlayerEntity player = client.player;

        if (player.hasStatusEffect(NeMuelchStatusEffects.SLIMED)) {
            renderOverlay(context, SLIME_OVERLAY_TEXTURE, 1.0f);
        }
    }

    @WrapOperation(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V"))
    private void drawAcidHeats(DrawContext instance, Identifier texture, int x, int y, int u, int v, int width, int height,
                               Operation<Void> original,
                               @Local(argsOnly = true) DrawContext context,
                               @Local(argsOnly = true) InGameHud.HeartType type,
                               @Local(ordinal = 0, argsOnly = true) boolean blinking,
                               @Local(ordinal = 1, argsOnly = true) boolean halfHeart) {
        ClientPlayerEntity player = client.player;
        if (type == InGameHud.HeartType.CONTAINER || player == null || !player.hasStatusEffect(NeMuelchStatusEffects.ACID_BURN)) {
            original.call(instance, texture, x, y, u, v, width, height);
            return;
        }
        int newU = 0;
        if (blinking) newU += 18;
        if (halfHeart) newU += 9;
        context.drawTexture(ACID_HEART_TEXTURES, x, y, newU, 0, 9, 9, 36, 9);
        // original.call(instance, ACID_HEART_TEXTURES, x, y, newU, 0, width, height);
    }
}
