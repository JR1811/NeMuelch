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
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.command.argument.ShaderNetworkingParameterArgumentType;
import net.shirojr.nemuelch.compat.satin.util.NetworkingParameter;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShaderServerCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_USER =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by a Player"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("shader")
                .then(literal("fade")
                        .then(literal("toBlack")
                                .then(argument("duration", IntegerArgumentType.integer(0))
                                        .executes(context -> ShaderServerCommand.handleDynamicFade(context, false, NetworkIdentifiers.FADE_TO_BLACK))
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> ShaderServerCommand.handleDynamicFade(context, true, NetworkIdentifiers.FADE_TO_BLACK))
                                        )
                                )
                        )
                        .then(literal("fromBlack")
                                .then(argument("duration", IntegerArgumentType.integer(0))
                                        .executes(context -> ShaderServerCommand.handleDynamicFade(context, false, NetworkIdentifiers.FADE_FROM_BLACK))
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> ShaderServerCommand.handleDynamicFade(context, true, NetworkIdentifiers.FADE_FROM_BLACK))
                                        )
                                )
                        )
                        .then(literal("set")
                                .then(argument("amount", FloatArgumentType.floatArg(0f, 1f))
                                        .executes(context -> ShaderServerCommand.handleStaticFade(context, false))
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> ShaderServerCommand.handleStaticFade(context, true))
                                        )
                                )
                        )
                )
                .then(literal("crimson")
                        .then(literal("set")
                                .then(argument("intensity", FloatArgumentType.floatArg(0f, 1f))
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            if (player == null) throw NO_USER.create();
                                            return ShaderServerCommand.setCrimsonIntensity(context, new HashSet<>(List.of(player)));
                                        })
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> {
                                                    HashSet<ServerPlayerEntity> targets = new HashSet<>(EntityArgumentType.getPlayers(context, "targets"));
                                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                                    if (player != null) targets.add(player);
                                                    return ShaderServerCommand.setCrimsonIntensity(context, targets);
                                                })
                                        )
                                )
                        )
                        .then(argument("limit", FloatArgumentType.floatArg())
                                .then(literal("near")
                                        .executes(ShaderServerCommand::setNearLimit)
                                )
                                .then(literal("far")
                                        .executes(ShaderServerCommand::setFarLimit)
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int setFarLimit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw NO_USER.create();
        float limit = FloatArgumentType.getFloat(context, "limit");

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(limit);
        buf.writeVarInt(NetworkingParameter.CLAMP_2.ordinal());
        ServerPlayNetworking.send(player, NetworkIdentifiers.GENERAL_SHADER_PARAMETER_SYNC, buf);

        context.getSource().sendFeedback(() -> Text.literal("Near limit: " + limit), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setNearLimit(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw NO_USER.create();
        float limit = FloatArgumentType.getFloat(context, "limit");

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(limit);
        buf.writeVarInt(NetworkingParameter.CLAMP_1.ordinal());
        ServerPlayNetworking.send(player, NetworkIdentifiers.GENERAL_SHADER_PARAMETER_SYNC, buf);

        context.getSource().sendFeedback(() -> Text.literal("Near limit: " + limit), true);
        return Command.SINGLE_SUCCESS;
    }


    private static int setCrimsonIntensity(CommandContext<ServerCommandSource> context, HashSet<ServerPlayerEntity> targets) {
        float intensity = FloatArgumentType.getFloat(context, "intensity");

        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(intensity);
            ServerPlayNetworking.send(target, NetworkIdentifiers.CRIMSON_STATIC, buf);
        }

        context.getSource().sendFeedback(() -> Text.literal("Set Crimson Shader Intensity: " + intensity), true);
        return Command.SINGLE_SUCCESS;
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
