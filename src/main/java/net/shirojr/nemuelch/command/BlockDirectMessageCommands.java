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
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
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
        dispatcher.register(literal("preventDirectMessages")
                .then(argument("enable", BoolArgumentType.bool())
                        .executes(context -> BlockDirectMessageCommands.toggle(context, null))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context ->
                                        BlockDirectMessageCommands.toggle(context, EntityArgumentType.getPlayers(context, "targets")
                                        )
                                )
                        )
                )
        );
    }

    private static int toggle(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> targetCollection) throws CommandSyntaxException {
        HashSet<ServerPlayerEntity> targets = new HashSet<>();
        if (targetCollection == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) throw NO_TARGET.create();
            targets.add(player);
        } else {
            targets.addAll(targetCollection);
        }
        boolean enabled = BoolArgumentType.getBool(context, "enable");
        targets.forEach(serverPlayer -> {
            MiscEntityComponent component = MiscEntityComponent.get(serverPlayer);
            component.setBlocksDirectMessages(enabled);
            serverPlayer.sendMessage(Text.literal("Set Direct Message block to " + enabled));
        });
        if (targetCollection != null) {
            context.getSource().sendFeedback(() -> Text.literal("Set Direct Message block to %s for targets".formatted(enabled)), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
