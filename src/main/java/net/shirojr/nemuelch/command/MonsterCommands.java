package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.init.NeMuelchCustomRegistries;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MonsterCommands implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher,
                         CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(literal("monster").requires(source -> source.hasPermissionLevel(2))
                .then(literal("set")
                        .then(argument("type", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, NeMuelchCustomRegistries.MONSTERS_KEY))
                                .executes(context -> MonsterCommands.setMonsterType(context, new ArrayList<>()))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context ->
                                                MonsterCommands.setMonsterType(context, EntityArgumentType.getPlayers(context, "targets"))
                                        )
                                )
                        )
                )
                .then(literal("get")
                        .executes(context -> MonsterCommands.printMonsterType(context, new ArrayList<>()))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context ->
                                        MonsterCommands.printMonsterType(context, EntityArgumentType.getPlayers(context, "targets"))
                                )
                        )
                )
        );
    }

    private static int printMonsterType(CommandContext<ServerCommandSource> context, @NotNull Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        if (targets.isEmpty()) {
            ServerPlayerEntity player = source.getPlayer();
            if (player == null) {
                throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
            }
            targets.add(player);
        }
        source.sendFeedback(() -> Text.literal("Targets contain following monster data:"), true);
        for (ServerPlayerEntity target : targets) {
            MonsterComponent component = MonsterComponent.get(target);
            component.getActiveType().ifPresentOrElse(type -> {
                Identifier id = NeMuelchCustomRegistries.MONSTERS.getId(type);
                source.sendFeedback(() -> Text.literal(target.getName().getString() + ": " + id), true);
                type.printExtraCommandInfo(source);
            }, () -> source.sendFeedback(() -> Text.literal(target.getName().getString() + ": none"), true));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setMonsterType(CommandContext<ServerCommandSource> context, @NotNull Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) {
                throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
            }
            targets.add(player);
        }
        RegistryEntry.Reference<AbstractMonsterType> type = RegistryEntryArgumentType.getRegistryEntry(
                context, "type", NeMuelchCustomRegistries.MONSTERS_KEY
        );
        for (ServerPlayerEntity target : targets) {
            MonsterComponent component = MonsterComponent.get(target);
            component.setActiveType(type.value());
        }
        context.getSource().sendFeedback(() -> Text.literal("Applied %s Monster Type to targets".formatted(type.registryKey().getValue())), true);
        return Command.SINGLE_SUCCESS;
    }
}