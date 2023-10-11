package net.shirojr.nemuelch.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.shirojr.nemuelch.util.helper.SleepEventHelper;
import net.shirojr.nemuelch.world.PersistentWorldData;

import java.util.ArrayList;

public class PersistentWorldDataCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("nemuelch").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(CommandManager.literal("world").then(CommandManager.literal("persistentData").then(CommandManager.literal("reset")
                                .executes(PersistentWorldDataCommand::runReset))
                        .then(CommandManager.literal("display").executes(PersistentWorldDataCommand::runListEntries)))));
    }

    private static int runReset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);
        persistentWorldData.usedSleepEventEntries.clear();

        context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.world_data.reset"), true);
        return 1;
    }

    private static int runListEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        PersistentWorldData persistentWorldData = PersistentWorldData.getServerState(server);
        for (var entry : persistentWorldData.usedSleepEventEntries) {
            PlayerEntity player = server.getPlayerManager().getPlayer(entry.playerUuid());

            Text playerText = player == null ? new LiteralText("[" + entry.playerUuid().toString() + "]")
                    .styled(style -> style.withColor(Formatting.AQUA)) :
                    new LiteralText("[" + player.getName().getString() + "]")
                            .styled(style -> style.withColor(Formatting.AQUA));

            context.getSource().sendFeedback(playerText, true);

            ArrayList<Text> lines = SleepEventHelper.getEntry(entry.sleepEvent());
            for (Text line : lines) {
                if (line.getString().isBlank()) continue;
                if (line.getString().equals(SleepEventHelper.nailLines().getString())) continue;
                context.getSource().sendFeedback(line, true);
            }
        }
        context.getSource().sendFeedback(
                new LiteralText(persistentWorldData.usedSleepEventEntries.size() + " entries have been used, and won't appear again")
                        .styled(style -> style.withColor(Formatting.GREEN)), true);

        return 1;
    }
}
