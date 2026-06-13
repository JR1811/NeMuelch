package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.*;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.shirojr.nemuelch.command.argument.TicketLevelArgumentType;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.mixin.access.ChunkTicketManagerAccess;
import net.shirojr.nemuelch.mixin.access.ServerChunkManagerAccess;
import net.shirojr.nemuelch.mixin.access.ThreadedAnvilChunkStorageAccess;
import net.shirojr.nemuelch.util.constants.TicketMapper;
import net.shirojr.nemuelch.util.data.WorldChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ServerUtilCommands implements CommandRegistrationCallback {
    private static final int MAX_SEARCH_RESULT_AMOUNT = NeMuelchConfigInit.CONFIG.serverUtilCommandMaxResultSize;
    private static final SimpleCommandExceptionType NO_ENTRIES =
            new SimpleCommandExceptionType(Text.literal("No entries"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("server")
                .then(literal("info")
                        .then(literal("chunkTickets")
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
                        .then(literal("entities")
                                .then(literal("countAll")
                                        .executes(ServerUtilCommands::printEntityCount)
                                )
                                .then(literal("entityToChunkRatio")
                                        .then(argument("entityTypes", EntityArgumentType.entities())
                                                .executes(ServerUtilCommands::printEntityToChunkRatio)
                                        )
                                )
                                .then(literal("mostEntitiesInChunk")
                                        .executes(context ->
                                                ServerUtilCommands.mostEntitiesInChunk(context, null)
                                        )
                                        .then(argument("entityTypes", EntityArgumentType.entities())
                                                .executes(context ->
                                                        ServerUtilCommands.mostEntitiesInChunk(context, EntityArgumentType.getEntities(context, "entityTypes"))
                                                )
                                        )
                                )
                        )
                        .then(literal("blockEntities")
                                .then(literal("mostBlockEntitiesInChunk")
                                        .executes(context ->
                                                ServerUtilCommands.mostBlockEntitiesInChunk(context, null)
                                        )
                                        .then(argument("blockCriteria", BlockPredicateArgumentType.blockPredicate(registryAccess))
                                                .executes(context ->
                                                        ServerUtilCommands.mostBlockEntitiesInChunk(context, BlockPredicateArgumentType.getBlockPredicate(context, "blockCriteria"))
                                                )
                                        )
                                )
                        )
                )
                .build();
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int mostBlockEntitiesInChunk(CommandContext<ServerCommandSource> context, @Nullable Predicate<CachedBlockPosition> blockFilter) throws CommandSyntaxException {
        Object2IntOpenHashMap<WorldChunkPos> blockEntitiesPerChunk = new Object2IntOpenHashMap<>();
        Predicate<CachedBlockPosition> searchCriteria = blockFilter != null ? blockFilter : unused -> true;

        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            ServerChunkManager chunkManager = world.getChunkManager();
            Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders = ((ThreadedAnvilChunkStorageAccess) chunkManager.threadedAnvilChunkStorage).getCurrentChunkHolders();

            currentChunkHolders.values().stream()
                    .map(ChunkHolder::getWorldChunk)
                    .filter(Objects::nonNull)
                    .forEach(worldChunk -> worldChunk.getBlockEntities().entrySet().stream()
                            .filter(entry -> searchCriteria.test(new CachedBlockPosition(world, entry.getKey(), false)))
                            .forEach(entry -> blockEntitiesPerChunk.mergeInt(new WorldChunkPos(world, worldChunk.getPos()), 1, Integer::sum))
                    );
        }
        if (blockEntitiesPerChunk.isEmpty()) throw NO_ENTRIES.create();
        context.getSource().sendFeedback(() -> Text.literal("Chunks with most loaded Block Entities"), true);

        blockEntitiesPerChunk.object2IntEntrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getIntValue(), a.getIntValue()))
                .limit(MAX_SEARCH_RESULT_AMOUNT)
                .forEach(chunkEntry -> {
                    WorldChunkPos worldChunk = chunkEntry.getKey();
                    ChunkPos chunkPos = worldChunk.pos();
                    int centeredX = chunkPos.x * 16 + 8;
                    int centeredZ = chunkPos.z * 16 + 8;
                    int surfaceY = worldChunk.world().getTopY(Heightmap.Type.MOTION_BLOCKING, centeredX, centeredZ);
                    Identifier dimensionId = worldChunk.world().getRegistryKey().getValue();

                    MutableText posText = Text.literal("[%d, %d]".formatted(chunkPos.x, chunkPos.z))
                            .styled(style -> style
                                    .withClickEvent(
                                            new ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/execute in %s run tp @s %d %d %d".formatted(dimensionId, centeredX, surfaceY, centeredZ)
                                            )
                                    )
                                    .withHoverEvent(
                                            new HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Text.literal("Click to teleport to chunk center")
                                            )
                                    )
                                    .withColor(Formatting.AQUA)
                            );
                    MutableText line = Text.literal(" ")
                            .append(posText)
                            .append(Text.literal(" - %d Block Entities (%s)".formatted(
                                    chunkEntry.getIntValue(), chunkEntry.getKey().world().getRegistryKey().getValue())
                            ).styled(style -> style.withColor(Formatting.GRAY)));
                    context.getSource().sendFeedback(() -> line, true);
                });
        return Command.SINGLE_SUCCESS;
    }

    private static int mostEntitiesInChunk(CommandContext<ServerCommandSource> context, @Nullable Collection<? extends Entity> entityTypes) throws CommandSyntaxException {
        Set<EntityType<?>> searchCriteria = entityTypes == null ? null : entityTypes.stream()
                                                                         .map(Entity::getType)
                                                                         .collect(Collectors.toSet());
        Object2IntOpenHashMap<WorldChunkPos> entitiesPerChunk = new Object2IntOpenHashMap<>();

        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            for (Entity entry : world.iterateEntities()) {
                if (searchCriteria != null && !searchCriteria.contains(entry.getType())) continue;
                entitiesPerChunk.mergeInt(new WorldChunkPos(world, entry.getChunkPos()), 1, Integer::sum);
            }
        }
        if (entitiesPerChunk.isEmpty()) throw NO_ENTRIES.create();
        context.getSource().sendFeedback(() -> Text.literal("Chunks with most loaded Entities"), true);

        entitiesPerChunk.object2IntEntrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getIntValue(), a.getIntValue()))
                .limit(MAX_SEARCH_RESULT_AMOUNT)
                .forEach(chunkEntry -> {
                            WorldChunkPos worldChunk = chunkEntry.getKey();
                            ChunkPos chunkPos = worldChunk.pos();
                            int centeredX = chunkPos.x * 16 + 8;
                            int centeredZ = chunkPos.z * 16 + 8;
                            int surfaceY = worldChunk.world().getTopY(Heightmap.Type.MOTION_BLOCKING, centeredX, centeredZ);
                            Identifier dimensionId = worldChunk.world().getRegistryKey().getValue();

                            MutableText posText = Text.literal("[%d, %d]".formatted(chunkPos.x, chunkPos.z))
                                    .styled(style -> style
                                            .withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.RUN_COMMAND,
                                                            "/execute in %s run tp @s %d %d %d".formatted(dimensionId, centeredX, surfaceY, centeredZ)
                                                    )
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Text.literal("Click to teleport to chunk center")
                                                    )
                                            )
                                            .withColor(Formatting.AQUA)
                                    );
                            MutableText line = Text.literal(" ")
                                    .append(posText)
                                    .append(Text.literal(" - %d Entities (%s)".formatted(
                                            chunkEntry.getIntValue(), chunkEntry.getKey().world().getRegistryKey().getValue())
                                    ).styled(style -> style.withColor(Formatting.GRAY)));
                            context.getSource().sendFeedback(() -> line, true);
                        }
                );

        return Command.SINGLE_SUCCESS;
    }

    private static int printEntityToChunkRatio(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Set<EntityType<?>> searchCriteria = EntityArgumentType.getEntities(context, "entityTypes").stream()
                .map(Entity::getType)
                .collect(Collectors.toSet());
        HashMap<EntityType<?>, HashSet<ChunkPos>> typeToChunks = new HashMap<>();
        Object2IntOpenHashMap<EntityType<?>> typeToCount = new Object2IntOpenHashMap<>();

        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!searchCriteria.contains(entity.getType())) continue;
                typeToChunks.computeIfAbsent(entity.getType(), entityType -> new HashSet<>()).add(entity.getChunkPos());
                typeToCount.mergeInt(entity.getType(), 1, Integer::sum);
            }
        }
        if (typeToCount.isEmpty()) throw NO_ENTRIES.create();
        context.getSource().sendFeedback(() -> Text.literal("Active Entity to Chunk Spread"), true);

        typeToCount.object2IntEntrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getIntValue(), a.getIntValue()))
                .limit(MAX_SEARCH_RESULT_AMOUNT)
                .forEach(entry -> {
                    EntityType<?> entityType = entry.getKey();
                    int chunkAmount = typeToChunks.get(entityType).size();
                    int entityCount = entry.getIntValue();
                    double ratio = MathHelper.clamp((double) chunkAmount / entityCount, 0, 1);
                    Identifier id = Registries.ENTITY_TYPE.getId(entityType);
                    context.getSource().sendFeedback(() -> Text.literal("%s %s (%s) across %s chunks (%s%% spread)".formatted(
                            entityCount, entityType.getName().getString(), id, chunkAmount, (int) (ratio * 100)
                    )).styled(style -> style.withColor(Formatting.GRAY)), true);
                });
        return Command.SINGLE_SUCCESS;
    }

    private static int printEntityCount(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Object2IntOpenHashMap<EntityType<?>> entityCounter = new Object2IntOpenHashMap<>();
        for (ServerWorld world : context.getSource().getServer().getWorlds()) {
            world.iterateEntities().forEach(entity ->
                    entityCounter.mergeInt(entity.getType(), 1, Integer::sum));
        }
        Object2IntMap.FastEntrySet<EntityType<?>> entries = entityCounter.object2IntEntrySet();
        if (entries.isEmpty()) throw NO_ENTRIES.create();
        context.getSource().sendFeedback(() -> Text.literal("Active Entity count"), true);

        entries.stream()
                .sorted((a, b) -> Integer.compare(b.getIntValue(), a.getIntValue()))
                .limit(MAX_SEARCH_RESULT_AMOUNT)
                .forEach(entityTypeEntry -> {
                    Identifier id = Registries.ENTITY_TYPE.getId(entityTypeEntry.getKey());
                    context.getSource().sendFeedback(() -> Text.literal("%s (%s): %s".formatted(
                            entityTypeEntry.getKey().getName().getString(), id, entityTypeEntry.getIntValue())
                    ).styled(style -> style.withColor(Formatting.GRAY)), true);
                });

        return Command.SINGLE_SUCCESS;
    }

    private static int printChunkTicketTypes(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();

        for (ServerWorld world : server.getWorlds()) {
            ChunkTicketManager ticketManager = ((ServerChunkManagerAccess) world.getChunkManager()).getTicketManager();
            Long2ObjectOpenHashMap<SortedArraySet<ChunkTicket<?>>> ticketsByPosition = ((ChunkTicketManagerAccess) ticketManager).getTicketsByPosition();
            if (ticketsByPosition.isEmpty()) continue;

            Map<String, Integer> byTypeAndLevel = new TreeMap<>();
            for (var entry : ticketsByPosition.long2ObjectEntrySet()) {
                for (ChunkTicket<?> ticket : entry.getValue()) {
                    int ticketLevel = ticket.getLevel();
                    String key = ticket.getType().toString() + "(level " + ticketLevel + " - " + TicketMapper.fromLevel(ticketLevel).asString() + ")";
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
            int count = 0;
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
                count += 1;
            }
            if (count > 40) {
                context.getSource().sendFeedback(() ->
                        Text.literal("Too many entries shown. Check Console for full view").styled(style -> style.withColor(Formatting.RED)
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
