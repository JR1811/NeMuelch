package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.shirojr.nemuelch.compat.cca.implementation.DirectMessagesHandlerComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlockDirectMessageCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_TARGET =
            new SimpleCommandExceptionType(Text.literal("No target specified"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("dm")
                .then(literal("block")
                        .then(literal("all")
                                .then(argument("enable", BoolArgumentType.bool())
                                        .executes(context -> BlockDirectMessageCommands.toggleAll(context, null))
                                        .then(argument("users", EntityArgumentType.players())
                                                .requires(source -> source.hasPermissionLevel(2))
                                                .executes(context ->
                                                        BlockDirectMessageCommands.toggleAll(context, EntityArgumentType.getPlayers(context, "users")
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("targets")
                                .then(argument("enable", BoolArgumentType.bool())
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context -> BlockDirectMessageCommands.toggleTargets(context, null))
                                                .then(argument("users", EntityArgumentType.players())
                                                        .requires(source -> source.hasPermissionLevel(2))
                                                        .executes(context ->
                                                                BlockDirectMessageCommands.toggleTargets(context, EntityArgumentType.getPlayers(context, "users")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int toggleTargets(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> usersCollection) throws CommandSyntaxException {
        HashSet<ServerPlayerEntity> users = new HashSet<>();
        if (usersCollection == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) throw NO_TARGET.create();
            users.add(player);
        } else {
            users.addAll(usersCollection);
        }
        boolean enabled = BoolArgumentType.getBool(context, "enable");
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity user : users) {
            DirectMessagesHandlerComponent component = DirectMessagesHandlerComponent.get(user);
            component.modifyBlockedTargets(uuids -> {
                if (enabled) {
                    targets.forEach(target -> {
                        uuids.add(target.getUuid());
                        context.getSource().sendFeedback(() -> Text.literal("Set Blocked Direct messages of %s from %s to %s"
                                .formatted(user.getName().getString(), target.getName().getString(), true)), true);
                    });
                } else {
                    targets.forEach(target -> {
                        uuids.remove(target.getUuid());
                        context.getSource().sendFeedback(() -> Text.literal("Set Blocked Direct messages of %s from %s to %s"
                                .formatted(user.getName().getString(), target.getName().getString(), false)), true);
                    });
                }
            });
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleAll(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> usersCollection) throws CommandSyntaxException {
        HashSet<ServerPlayerEntity> users = new HashSet<>();
        if (usersCollection == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) throw NO_TARGET.create();
            users.add(player);
        } else {
            users.addAll(usersCollection);
        }
        boolean enabled = BoolArgumentType.getBool(context, "enable");
        users.forEach(serverPlayer -> {
            DirectMessagesHandlerComponent component = DirectMessagesHandlerComponent.get(serverPlayer);
            component.setBlocksAllMessages(enabled);
            serverPlayer.sendMessage(Text.literal("Set Direct Message block to " + enabled));
        });
        if (usersCollection != null) {
            context.getSource().sendFeedback(() -> Text.literal("Set Direct Message block to %s for targets".formatted(enabled)), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
