package net.shirojr.nemuelch.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    private static final Identifier SLIME_OVERLAY_TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/misc/slime_overlay.png");
    @Unique
    private static final Identifier ICONS_TEXTURE = new Identifier(NeMuelch.MOD_ID, "textures/gui/icons.png");

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getFrozenTicks()I"))
    private void nemuelch$renderAdditionalOverlays(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (client.player == null) return;
        ClientPlayerEntity player = client.player;

        if (player.hasStatusEffect(NeMuelchEffects.SLIMED)) {
            renderOverlay(context, SLIME_OVERLAY_TEXTURE, 1.0f);
        }
    }

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V", ordinal = 0, remap = false))
    private void renderPullUpIcon(DrawContext context, CallbackInfo ci) {
        Entity target = this.client.targetedEntity;
        if (!(target instanceof LivingEntity targetEntity)) return;

        if (targetEntity.isOnGround()) return;
        if (targetEntity.fallDistance > 0) return;

        //FIXME: doesnt render?
        int x = scaledWidth / 2 - 4;
        int y = scaledHeight / 2 - 17;
        context.drawTexture(ICONS_TEXTURE, x, y, 1, 1, 8, 13);
    }
}
