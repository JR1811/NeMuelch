package net.shirojr.nemuelch.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;
import net.shirojr.nemuelch.world.PersistentWorldData;

import java.util.ArrayList;

@SuppressWarnings({"unused", "RedundantThrows"})
public class SpecialSleepEventCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("nemuelch").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.literal("world").then(CommandManager.literal("persistentData")
                        .then(CommandManager.literal("reset")
                                .executes(SpecialSleepEventCommand::runResetAll)
                                .then(CommandManager.argument("index", IntegerArgumentType.integer())
                                        .executes(context -> SpecialSleepEventCommand.runResetSpecific(context, IntegerArgumentType.getInteger(context, "index")))))    //TODO: needs testing
                        .then(CommandManager.literal("display").executes(SpecialSleepEventCommand::runListEntries)))));
    }

    private static int runResetAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);
        persistentWorldData.usedSleepEventEntries.clear();

        context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.world_data.reset"), true);
        return 1;
    }

    private static int runResetSpecific(CommandContext<ServerCommandSource> context, int commandInputId) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);

        for (int i = 0; i < persistentWorldData.usedSleepEventEntries.size(); i++) {
            SleepEventHelper.SleepEventDataEntry entry = persistentWorldData.usedSleepEventEntries.get(i);
            if (entry.sleepEventIndex() == commandInputId) {
                persistentWorldData.usedSleepEventEntries.remove(i);
                context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.world_data.reset_specific", commandInputId), true);
                return 1;
            }
        }
        context.getSource().sendError(new TranslatableText("feedback.nemuelch.world_data.reset_specific_error", commandInputId));
        return 0;
    }

    private static int runListEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);
        for (var entry : persistentWorldData.usedSleepEventEntries) {
            MutableText playerText = new LiteralText(entry.playerName()).styled(style -> style.withColor(Formatting.AQUA));
            Text entryInformation = playerText.append(new LiteralText(" activated entry Index: " + entry.sleepEventIndex()));
            context.getSource().sendFeedback(entryInformation, true);

            ArrayList<Text> lines = SleepEventHelper.getEntry(entry.sleepEventIndex());
            for (Text line : lines) {
                if (line.getString().isBlank()) continue;
                context.getSource().sendFeedback(line, true);
            }
        }

        String leftOverEntries;
        if (persistentWorldData.usedSleepEventEntries.size() > 0) {
            leftOverEntries = String.format("%s entries have been used, and won't appear again.", persistentWorldData.usedSleepEventEntries.size());
        } else leftOverEntries = "No entries have been used in this world.";

        context.getSource().sendFeedback(new LiteralText(leftOverEntries).styled(style -> style.withColor(Formatting.GREEN)), true);
        return 1;
    }
}
