package net.shirojr.nemuelch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.NeMuelchClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    private void renderObfuscatedSkin(int i, Identifier identifier, Operation<Void> original, @Local GameProfile playerProfile) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.isSpectator()) {
            original.call(i, identifier);
            return;
        }
        Optional<Boolean> isObfuscated = Optional.ofNullable(NeMuelchClient.OBFUSCATED_CACHE.get(playerProfile.getId()));
        if (isObfuscated.isEmpty() || !isObfuscated.get()) {
            original.call(i, identifier);
            return;
        }
        RenderSystem.setShaderTexture(0, new Identifier(NeMuelch.MOD_ID, "textures/entity/obfuscated_skin.png"));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;"))
    private Text renderObfuscatedName(PlayerListHud instance, PlayerListEntry entry, Operation<Text> original) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.isSpectator()) {
            return original.call(instance, entry);
        }
        Optional<Boolean> isObfuscated = Optional.ofNullable(NeMuelchClient.OBFUSCATED_CACHE.get(entry.getProfile().getId()));
        if (isObfuscated.isEmpty() || !isObfuscated.get()) {
            return original.call(instance, entry);
        }
        MutableText name = original.call(instance, entry).copy();
        return name.setStyle(name.getStyle().withFormatting(Formatting.OBFUSCATED));
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;isEncrypted()Z"))
    private boolean enabledForTestEnv(boolean original) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) return true;
        return original;
    }
}
