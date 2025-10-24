package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkTrackerComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlightCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_BLIGHT =
            new SimpleCommandExceptionType(Text.literal("Requested Data does not contain any Blight"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("blight").requires(source -> source.hasPermissionLevel(2))
                .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                                .then(argument("type", BlightType.ArgumentType.blightType())
                                        .then(literal("add")
                                                .executes(BlightCommands::addType)
                                        )
                                        .then(literal("clear")
                                                .then(literal("chunks")
                                                        .executes(BlightCommands::clearChunks)
                                                )
                                        )
                                )
                        )
                )
                .then(argument("single", BlockPosArgumentType.blockPos())
                        .then(literal("clear")
                                .executes(BlightCommands::clearSingle)
                        )
                        .then(literal("info")
                                .executes(BlightCommands::info)
                        )
                )
                .then(literal("clear")
                        .then(literal("all")
                                .executes(BlightCommands::clearAll)
                        )
                )
                .then(literal("info")
                        .then(literal("all")
                                .executes(BlightCommands::infoAll)
                        )
                )
        );
    }

    private static int infoAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        Set<ChunkPos> allBlightedChunks = BlightChunkTrackerComponent.get(world).getAllBlightedChunks();
        if (allBlightedChunks.isEmpty()) {
            throw NO_BLIGHT.create();
        }
        context.getSource().sendFeedback(() -> Text.literal("Chunks with Blights: "), true);
        for (ChunkPos entry : allBlightedChunks) {
            context.getSource().sendFeedback(() -> Text.literal("ChunkPos: %s".formatted(entry)), true);
        }
        context.getSource().sendFeedback(() -> Text.literal("%s Chunks contain blight data".formatted(allBlightedChunks.size())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearAll(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlightChunkTrackerComponent.get(world).clearBlightedChunks(world);
        context.getSource().sendFeedback(() -> Text.literal("Cleared all partial and complete Chunk Blights in current world"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "single");
        Chunk chunk = world.getChunk(pos);

        Optional<BlightChunkComponent> component = BlightChunkComponent.maybeGet(chunk);
        if (component.isEmpty() || component.get().isEmpty()) {
            throw NO_BLIGHT.create();
        } else {
            EnumSet<BlightType> completeChunkBlights = component.get().getCompleteChunkBlights();
            context.getSource().sendFeedback(() -> Text.literal("Complete Blights: %s".formatted(completeChunkBlights)), true);
            for (BlightType type : BlightType.CACHED_VALUES) {
                int blightPosCount = component.get().getBlightPosCount(type);
                context.getSource().sendFeedback(() -> Text.literal("%s with %s Blocks blighted".formatted(type, blightPosCount)), true);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearSingle(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "single");
        Chunk chunk = world.getChunk(pos);

        BlightChunkComponent.maybeGet(chunk).ifPresent(component -> component.clear(true, true, true));

        context.getSource().sendFeedback(() -> Text.literal("Cleared %s of any Blight".formatted(chunk)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearChunks(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlockPos from = BlockPosArgumentType.getBlockPos(context, "from");
        BlockPos to = BlockPosArgumentType.getBlockPos(context, "to");
        Box box = new Box(from, to);
        HashSet<Chunk> chunks = new HashSet<>();
        BlightType type = BlightType.ArgumentType.getBlockRotation(context, "type");

        BlockPos.stream(box).forEach(pos -> chunks.add(world.getChunk(pos)));
        for (Chunk chunk : chunks) {
            Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(chunk);
            if (blightChunkComponent.isEmpty()) continue;
            BlightChunkComponent component = blightChunkComponent.get();
            component.clear(type);
            context.getSource().sendFeedback(
                    () -> Text.literal("Cleared Chunk of ChunkPos [x: %s| z: %s]".formatted(chunk.getPos().x, chunk.getPos().z)),
                    true
            );
        }
        context.getSource().sendFeedback(
                () -> Text.literal("Removed all Blight data from %s chunks".formatted(chunks.size())),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int addType(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlockPos from = BlockPosArgumentType.getBlockPos(context, "from");
        BlockPos to = BlockPosArgumentType.getBlockPos(context, "to");
        BlightType type = BlightType.ArgumentType.getBlockRotation(context, "type");
        boolean blightedAnything = false;
        Box box = new Box(from, to);

        for (BlockPos pos : BlockPos.iterate(from, to)) {
            Chunk chunk = world.getChunk(pos);
            Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(chunk);
            if (blightChunkComponent.isEmpty()) continue;
            BlightChunkComponent component = blightChunkComponent.get();
            if (component.getCompleteChunkBlights().contains(type)) continue;
            blightedAnything = component.addBlightsToPos(pos, Set.of(type));
        }
        if (blightedAnything) {
            context.getSource().sendFeedback(() -> Text.literal("Added %s to all BlockPos in %s".formatted(type, box)), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Noting was blighted"), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
