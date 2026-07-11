package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.LoginComponent;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LoginCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_TARGETS =
            new SimpleCommandExceptionType(Text.literal("No targets found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("loginTracker")
                .then(literal("get")
                        .then(literal("byEntities")
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> LoginCommands.getLogin(context, EntityArgumentType.getPlayers(context, "targets")))
                                )
                        )
                        .then(literal("byUuid")
                                .then(argument("UUID", UuidArgumentType.uuid())
                                        .executes(context -> LoginCommands.getLogin(context, UuidArgumentType.getUuid(context, "UUID")))
                                )
                        )
                )
                .then(literal("list")
                        .executes(context -> LoginCommands.printListView(context, 20))
                        .then(argument("maxEntries", IntegerArgumentType.integer(1))
                                .executes(context -> LoginCommands.printListView(context, IntegerArgumentType.getInteger(context, "maxEntries")))
                        )
                )
                .then(literal("clear")
                        .then(literal("entries")
                                .then(literal("byEntities")
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> LoginCommands.clearEntries(context, EntityArgumentType.getPlayers(context, "targets")))
                                        )
                                )
                                .then(literal("byUuid")
                                        .then(argument("UUID", UuidArgumentType.uuid())
                                                .executes(context -> LoginCommands.clearEntries(context, UuidArgumentType.getUuid(context, "UUID")))
                                        )
                                )
                        )
                        .then(literal("all")
                                .executes(LoginCommands::clearAllEntries)
                        )
                )
                .build();

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int clearAllEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        LoginComponent component = LoginComponent.get(context.getSource().getServer());
        if (component.isEmpty()) throw NO_TARGETS.create();
        component.clearAll();
        context.getSource().sendFeedback(() -> Text.literal("Cleared all Login entries"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearEntries(CommandContext<ServerCommandSource> context, UUID uuid) throws CommandSyntaxException {
        removeUuidResult(context, List.of(uuid));
        return Command.SINGLE_SUCCESS;
    }

    private static int clearEntries(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        List<UUID> clearTargets = new ArrayList<>();
        targets.forEach(player -> clearTargets.add(player.getUuid()));
        if (clearTargets.isEmpty()) throw NO_TARGETS.create();

        removeUuidResult(context, clearTargets);
        return Command.SINGLE_SUCCESS;
    }

    private static void removeUuidResult(CommandContext<ServerCommandSource> context, List<UUID> uuidList) throws CommandSyntaxException {
        if (uuidList.isEmpty()) throw NO_TARGETS.create();
        MinecraftServer server = context.getSource().getServer();
        LoginComponent component = LoginComponent.get(server);
        if (component.isEmpty()) throw NO_TARGETS.create();
        for (UUID uuid : uuidList) {
            String name = LoginComponent.getCachedPlayerNameOrUuid(server, uuid);
            if (component.clearLogin(uuid)) {
                context.getSource().sendFeedback(() -> Text.literal("Cleared login data of %s".formatted(name)), true);
            } else {
                context.getSource().sendFeedback(() -> Text.literal("Data not found for %s".formatted(name)), true);
            }
        }
    }

    private static int printListView(CommandContext<ServerCommandSource> context, int maxEntries) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("Requested login data:"), true);
        LoginComponent component = LoginComponent.get(source.getServer());
        if (component.isEmpty()) throw NO_TARGETS.create();
        component.getSortedByLoginTime().stream().limit(maxEntries).forEachOrdered(entry -> {
            String name = LoginComponent.getCachedPlayerNameOrUuid(source.getServer(), entry.getKey());
            source.sendFeedback(() -> Text.literal(name + ": " + LoginComponent.getFormattedTime(entry.getLongValue())), true);
        });
        return Command.SINGLE_SUCCESS;
    }

    private static int getLogin(CommandContext<ServerCommandSource> context, UUID uuid) throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> Text.literal("Requested login data:"), true);
        printUuidResult(context, List.of(uuid));
        return Command.SINGLE_SUCCESS;
    }

    private static int getLogin(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        List<UUID> uuidList = new ArrayList<>();
        targets.forEach(player -> uuidList.add(player.getUuid()));

        context.getSource().sendFeedback(() -> Text.literal("Requested login data:"), true);
        printUuidResult(context, uuidList);
        return Command.SINGLE_SUCCESS;
    }

    private static void printUuidResult(CommandContext<ServerCommandSource> context, List<UUID> uuidList) throws CommandSyntaxException {
        if (uuidList.isEmpty()) throw NO_TARGETS.create();
        MinecraftServer server = context.getSource().getServer();
        LoginComponent component = LoginComponent.get(server);
        if (component.isEmpty()) throw NO_TARGETS.create();
        for (UUID uuid : uuidList) {
            String name = LoginComponent.getCachedPlayerNameOrUuid(server, uuid);
            OptionalLong lastLogin = component.getLastLogin(uuid);
            String time = lastLogin.isEmpty() ? "No data" : LoginComponent.getFormattedTime(lastLogin.getAsLong());

            context.getSource().sendFeedback(() -> Text.literal(name + ": " + time), true);
        }
    }
}
