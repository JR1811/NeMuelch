package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.DirectMessagesHandlerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MessageCommand.class)
public abstract class MessageCommandMixin {
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendChatMessage(Lnet/minecraft/network/message/SentMessage;ZLnet/minecraft/network/message/MessageType$Parameters;)V"))
    private static void blockChatMessageSource(ServerCommandSource instance, SentMessage message, boolean filterMaskEnabled,
                                               MessageType.Parameters params, Operation<Void> original, @Local ServerPlayerEntity target) {
        ServerPlayerEntity source = instance.getPlayer();
        if (source == null) {
            original.call(instance, message, filterMaskEnabled, params);
            return;
        }
        DirectMessagesHandlerComponent component = DirectMessagesHandlerComponent.get(target);
        boolean forceSend = source.hasPermissionLevel(2) && (source.isCreative() || source.isSpectator());
        if (!component.isBlocked(source) || forceSend) {
            original.call(instance, message, filterMaskEnabled, params);
            return;
        }
        source.sendMessage(
                Text.translatable("info.nemuelch.direct_message.blocked", target.getName().getString()),
                true
        );
    }

    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendChatMessage(Lnet/minecraft/network/message/SentMessage;ZLnet/minecraft/network/message/MessageType$Parameters;)V"))
    private static void blockChatMessageTarget(ServerPlayerEntity instance, SentMessage message, boolean filterMaskEnabled,
                                               MessageType.Parameters params, Operation<Void> original, @Local(argsOnly = true) ServerCommandSource source) {
        ServerPlayerEntity sourcePlayer = source.getPlayer();
        if (sourcePlayer == null) {
            original.call(instance, message, filterMaskEnabled, params);
            return;
        }
        boolean forceSend = sourcePlayer.hasPermissionLevel(2) && (sourcePlayer.isCreative() || sourcePlayer.isSpectator());
        DirectMessagesHandlerComponent component = DirectMessagesHandlerComponent.get(instance);
        if (!component.isBlocked(sourcePlayer) || forceSend) {
            original.call(instance, message, filterMaskEnabled, params);
        }
    }
}
