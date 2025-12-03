package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FadeServerCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_USER =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by a Player"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("fade")
                .then(literal("toBlack")
                        .then(argument("duration", IntegerArgumentType.integer(0))
                                .executes(context -> FadeServerCommand.handleDynamicFade(context, false, NetworkIdentifiers.FADE_TO_BLACK))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> FadeServerCommand.handleDynamicFade(context, true, NetworkIdentifiers.FADE_TO_BLACK))
                                )
                        )
                )
                .then(literal("fromBlack")
                        .then(argument("duration", IntegerArgumentType.integer(0))
                                .executes(context -> FadeServerCommand.handleDynamicFade(context, false, NetworkIdentifiers.FADE_FROM_BLACK))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> FadeServerCommand.handleDynamicFade(context, true, NetworkIdentifiers.FADE_FROM_BLACK))
                                )
                        )
                )
                .then(literal("set")
                        .then(argument("amount", FloatArgumentType.floatArg(0f, 1f))
                                .executes(context -> FadeServerCommand.handleStaticFade(context, false))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> FadeServerCommand.handleStaticFade(context, true))
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int handleStaticFade(CommandContext<ServerCommandSource> context, boolean multipleTargets) throws CommandSyntaxException {
        float amount = FloatArgumentType.getFloat(context, "amount");
        List<ServerPlayerEntity> targets = new ArrayList<>();
        if (multipleTargets) {
            targets.addAll(EntityArgumentType.getPlayers(context, "targets"));
        } else if (context.getSource().getPlayer() != null) {
            targets.add(context.getSource().getPlayer());
        } else {
            throw NO_USER.create();
        }

        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(amount);
            ServerPlayNetworking.send(target, NetworkIdentifiers.FADE_STATIC, buf);
        }

        context.getSource().sendFeedback(() -> Text.literal("Applied internal Fade Shader operation"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleDynamicFade(CommandContext<ServerCommandSource> context, boolean multipleTargets, Identifier packetIdentifier) throws CommandSyntaxException {
        int duration = IntegerArgumentType.getInteger(context, "duration");
        List<ServerPlayerEntity> targets = new ArrayList<>();
        if (multipleTargets) {
            targets.addAll(EntityArgumentType.getPlayers(context, "targets"));
        } else if (context.getSource().getPlayer() != null) {
            targets.add(context.getSource().getPlayer());
        } else {
            throw NO_USER.create();
        }

        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(duration);
            ServerPlayNetworking.send(target, packetIdentifier, buf);
        }
        context.getSource().sendFeedback(() -> Text.literal("Applied internal Fade Shader operation"), true);
        return Command.SINGLE_SUCCESS;
    }
}
