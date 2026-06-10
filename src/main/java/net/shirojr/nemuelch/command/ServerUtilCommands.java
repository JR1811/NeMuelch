package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicket;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.shirojr.nemuelch.command.argument.TicketLevelArgumentType;
import net.shirojr.nemuelch.mixin.access.ChunkTicketManagerAccess;
import net.shirojr.nemuelch.mixin.access.ServerChunkManagerAccess;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ServerUtilCommands implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("server")
                .then(literal("info")
                        .then(literal("activeChunkTickets")
                                .then(argument("intMaxLevel", IntegerArgumentType.integer())
                                        .executes(context -> printCurrentTickets(context, IntegerArgumentType.getInteger(context, "intMaxLevel")))
                                )
                                .then(argument("enumMaxLevel", TicketLevelArgumentType.level())
                                        .executes(context -> printCurrentTickets(context, TicketLevelArgumentType.getLevel(context, "enumMaxLevel").getLevel()))
                                )
                        )
                        .then(literal("chunkTicketTypes")
                                .executes(ServerUtilCommands::printChunkTicketTypes)
                        )
                )
                .build();
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int printChunkTicketTypes(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();

        for (ServerWorld world : server.getWorlds()) {
            ChunkTicketManager ticketManager = ((ServerChunkManagerAccess) world.getChunkManager()).getTicketManager();
            Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition = ((ChunkTicketManagerAccess) ticketManager).getTicketsByPosition();
            if (ticketsByPosition.isEmpty()) continue;
            context.getSource().sendFeedback(() -> Text.literal("=== " + world.getRegistryKey().getValue() + " ==="), true);

            Map<String, Integer> byTypeAndLevel = new TreeMap<>();
            for (var entry : ticketsByPosition.long2ObjectEntrySet()) {
                for (ChunkTicket<?> ticket : entry.getValue()) {
                    String key = ticket.getType().toString() + "(level " + ticket.getLevel() + ")";
                    byTypeAndLevel.merge(key, 1, Integer::sum);
                }
            }
            if (byTypeAndLevel.isEmpty()) continue;

            context.getSource().sendFeedback(() -> Text.literal("=== " + world.getRegistryKey().getValue() + " ==="), true);

            byTypeAndLevel.forEach((key, count) ->
                    context.getSource().sendFeedback(() ->
                            Text.literal("  " + key + " x" + count), true));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int printCurrentTickets(CommandContext<ServerCommandSource> context, int maxLevel) {
        MinecraftServer server = context.getSource().getServer();

        for (ServerWorld world : server.getWorlds()) {
            ChunkTicketManager ticketManager = ((ServerChunkManagerAccess) world.getChunkManager()).getTicketManager();
            Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition = ((ChunkTicketManagerAccess) ticketManager).getTicketsByPosition();
            if (ticketsByPosition.isEmpty()) continue;
            context.getSource().sendFeedback(() -> Text.literal("=== " + world.getRegistryKey().getValue() + " ==="), true);

            var positionEntries = ticketsByPosition.long2ObjectEntrySet();
            for (var entry : positionEntries) {
                ChunkPos pos = new ChunkPos(entry.getLongKey());
                SortedArraySet<ChunkTicket<?>> tickets = entry.getValue();

                String output = tickets.stream()
                        .filter(chunkTicket -> chunkTicket.getLevel() <= maxLevel)
                        .map(t -> t.getType().toString() + ":" + t.getLevel())
                        .collect(Collectors.joining(", "));
                if (output.isEmpty()) continue;
                BlockPos chunkCenter = getChunkCenter(world, pos);
                MutableText chunkPosText = Text.literal("  [%d, %d]".formatted(pos.x, pos.z))
                        .styled(style -> style
                                .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s %d %d %d".formatted(chunkCenter.getX(), chunkCenter.getY(), chunkCenter.getZ()))
                                )
                                .withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport to chunk center"))
                                )
                                .withColor(Formatting.AQUA)
                        );
                MutableText line = Text.empty().append(chunkPosText).append(Text.literal(" %s".formatted(output)));
                context.getSource().sendFeedback(() -> line, true);
            }
            if (positionEntries.size() > 40) {
                context.getSource().sendFeedback(() ->
                        Text.literal("Too many entries shown. Check Console for full view").styled(style -> style.withColor(Formatting.DARK_RED)
                        ), true);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static BlockPos getChunkCenter(World world, ChunkPos chunkPos) {
        int x = chunkPos.x * 16 + 8;
        int z = chunkPos.z * 16 + 8;
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        return new BlockPos(x, y, z);
    }
}
