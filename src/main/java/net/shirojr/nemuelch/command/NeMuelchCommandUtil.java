package net.shirojr.nemuelch.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.literal;

public class NeMuelchCommandUtil {
    public static final String BASE_NODE_NAME = "nemuelch";
    public static final Predicate<ServerCommandSource> HIGHER_PERMISSION_LEVEL = source -> source.hasPermissionLevel(2);

    public static CommandNode<ServerCommandSource> getOrCreateNeMuelchNode(CommandDispatcher<ServerCommandSource> dispatcher) {
        CommandNode<ServerCommandSource> node = dispatcher.getRoot().getChild(BASE_NODE_NAME);
        if (node == null) {
            node = dispatcher.register(literal(BASE_NODE_NAME).requires(source -> source.hasPermissionLevel(2)));
        }
        return node;
    }
}
