package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.ExplosionRefillerComponent;

import static net.minecraft.server.command.CommandManager.literal;

public class ExplosionRefillerCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType EMPTY_BACKLOG =
            new SimpleCommandExceptionType(Text.literal("Backlog had no entries stored"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("explosionRefiller").requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                .then(literal("backlog")
                        .then(literal("size")
                                .executes(ExplosionRefillerCommands::backlogSize)
                        )
                        .then(literal("startAllNow")
                                .executes(ExplosionRefillerCommands::backlogStartAllNow)
                        )
                        .then(literal("clear")
                                .executes(ExplosionRefillerCommands::backlogClear)
                        )
                )
                .build();

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int backlogStartAllNow(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ExplosionRefillerComponent component = ExplosionRefillerComponent.get(context.getSource().getWorld());
        if (component.isEmpty()) {
            throw EMPTY_BACKLOG.create();
        }
        component.startAllNow();
        context.getSource().sendFeedback(() -> Text.literal("Adjusted start time of all entries based on start delay"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int backlogSize(CommandContext<ServerCommandSource> context) {
        ExplosionRefillerComponent component = ExplosionRefillerComponent.get(context.getSource().getWorld());
        context.getSource().sendFeedback(() -> Text.literal(component.sizeExplosionEntries() + " explosion entries are stored"), true);
        context.getSource().sendFeedback(() -> Text.literal(component.sizeCollectedBlocks() + " affected blocks are stored"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int backlogClear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ExplosionRefillerComponent component = ExplosionRefillerComponent.get(context.getSource().getWorld());
        if (component.isEmpty()) {
            throw EMPTY_BACKLOG.create();
        }
        component.clear();
        context.getSource().sendFeedback(() -> Text.literal("Cleared Explosion Refilling backlog"), true);
        return Command.SINGLE_SUCCESS;
    }
}
