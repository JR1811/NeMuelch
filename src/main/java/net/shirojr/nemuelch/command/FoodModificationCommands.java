package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.FoodModificationComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FoodModificationCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType INVALID_TARGET =
            new SimpleCommandExceptionType(Text.literal("No valid target found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("foodModification")
                .then(literal("clear")
                        .executes(context -> FoodModificationCommands.clear(context, null))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context ->
                                        FoodModificationCommands.clear(context, EntityArgumentType.getPlayers(context, "targets"))
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int clear(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> selectedTargets) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<ServerPlayerEntity> targets = new ArrayList<>();
        ServerPlayerEntity player = source.getPlayer();
        if (selectedTargets != null) targets.addAll(selectedTargets);
        else if (player != null) targets.add(player);
        else throw INVALID_TARGET.create();
        if (targets.isEmpty()) throw INVALID_TARGET.create();

        for (ServerPlayerEntity target : targets) {
            FoodModificationComponent component = FoodModificationComponent.get(target);
            if (component.isEmpty()) {
                context.getSource().sendFeedback(() -> Text.literal("%s had no Food Modification entries".formatted(target.getName().getString())), true);
                continue;
            }
            component.clearActiveModifiers();
            context.getSource().sendFeedback(() -> Text.literal("Cleared Food Modification queue for %s".formatted(target.getName().getString())), true);
        }

        return Command.SINGLE_SUCCESS;
    }


}
