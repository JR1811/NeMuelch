package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shirojr.nemuelch.compat.cca.component.ActCommandComponent;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;

import java.util.Collection;
import java.util.HashSet;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ActCommand implements CommandRegistrationCallback {
    public static double MAX_DISTANCE = NeMuelchConfigInit.CONFIG.actCommandMaxRange;

    private static final SimpleCommandExceptionType SOURCE_NO_PLAYER =
            new SimpleCommandExceptionType(Text.literal("Command not executed by player"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("act")
                .then(argument("content", StringArgumentType.string())
                        .executes(context -> ActCommand.runDefault(context, false))
                        .then(argument("targets", EntityArgumentType.players())
                                .executes(context ->  ActCommand.runTargets(context, false))
                        )
                )
                .then(literal("incognito").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("content", StringArgumentType.string())
                                .executes(context -> ActCommand.runDefault(context, true))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context ->  ActCommand.runTargets(context, true))
                                )
                        )
                )
        );
        dispatcher.register(literal("act-debug").requires(source -> source.hasPermissionLevel(2))
                .then(literal("stalk")
                        .then(argument("stalk", BoolArgumentType.bool())
                                .executes(ActCommand::runStalkToggle)
                        )
                )
        );
    }

    private static int runTargets(CommandContext<ServerCommandSource> context, boolean incognito) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw SOURCE_NO_PLAYER.create();
        }
        HashSet<ServerPlayerEntity> targetsInMaxRange = new HashSet<>();
        for (ServerPlayerEntity target : EntityArgumentType.getPlayers(context, "targets")) {
            if (player.squaredDistanceTo(target) > MAX_DISTANCE * MAX_DISTANCE) continue;
            targetsInMaxRange.add(target);
        }
        sendText(player, targetsInMaxRange, StringArgumentType.getString(context, "content"), incognito);
        return Command.SINGLE_SUCCESS;
    }

    private static int runDefault(CommandContext<ServerCommandSource> context, boolean incognito) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw SOURCE_NO_PLAYER.create();
        }
        Collection<ServerPlayerEntity> around = PlayerLookup.around(player.getServerWorld(), player.getPos(), MAX_DISTANCE);
        sendText(player, around, StringArgumentType.getString(context, "content"), incognito);
        return Command.SINGLE_SUCCESS;
    }

    private static int runStalkToggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw SOURCE_NO_PLAYER.create();
        }
        boolean stalk = BoolArgumentType.getBool(context, "stalk");

        ActCommandComponent actCommandComponent = ActCommandComponent.get(player);
        actCommandComponent.setStalkMode(stalk);

        context.getSource().sendFeedback(() -> Text.literal("Stalk all /act commands: " + actCommandComponent.enabledStalkMode()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void sendText(ServerPlayerEntity source, Collection<ServerPlayerEntity> targets, String content, boolean incognito) {
        HashSet<ServerPlayerEntity> receivers = new HashSet<>(targets);
        MinecraftServer server = source.getServer();
        if (server != null) {
            for (ServerPlayerEntity entry : PlayerLookup.all(server)) {
                if (!entry.hasPermissionLevel(2)) continue;
                ActCommandComponent actCommandComponent = ActCommandComponent.get(entry);
                if (!actCommandComponent.enabledStalkMode()) continue;
                receivers.add(entry);
            }
        }
        for (ServerPlayerEntity target : receivers) {
            String output = "";
            if (!incognito) {
                 output += "§6[%s]§r ".formatted(source.getName().getString());
            }
            output += content;
            MutableText text = Text.literal(output).formatted(Formatting.ITALIC);
            if (NeMuelchConfigInit.CONFIG.printActCommandInChat) {
                target.sendMessage(text, false);
            }
            if (NeMuelchConfigInit.CONFIG.printActCommandInActionBar) {
                target.sendMessage(text, true);
            }
        }
    }
}
