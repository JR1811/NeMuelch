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
import net.minecraft.world.chunk.Chunk;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlightCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_BLIGHT =
            new SimpleCommandExceptionType(Text.literal("Requested Data does not contain any Blight"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(literal("blight").requires(source -> source.hasPermissionLevel(2))
                .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                                .then(argument("type", BlightType.ArgumentType.blightType())
                                        .then(literal("add")
                                                .executes(BlightCommand::addType)
                                        )
                                )
                        )
                )
                .then(argument("singlePos", BlockPosArgumentType.blockPos())
                        .then(literal("clear")
                                .executes(BlightCommand::clear)
                        )
                        .then(literal("info")
                                .executes(BlightCommand::info)
                        )
                )
        );
    }

    private static int info(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "singlePos");
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

    private static int clear(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "singlePos");
        Chunk chunk = world.getChunk(pos);

        BlightChunkComponent.maybeGet(chunk).ifPresent(component -> component.clear(true, true));

        context.getSource().sendFeedback(() -> Text.literal("Cleared %s of any Blight".formatted(chunk)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addType(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        BlockPos from = BlockPosArgumentType.getBlockPos(context, "from");
        BlockPos to = BlockPosArgumentType.getBlockPos(context, "to");
        Box box = new Box(from, to);

        BlightType type = BlightType.ArgumentType.getBlockRotation(context, "type");

        BlockPos.stream(box).forEach(pos -> {
            Chunk chunk = world.getChunk(pos);
            Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(chunk);
            blightChunkComponent.ifPresent(component -> {
                if (component.getCompleteChunkBlights().contains(type)) {
                    return;
                }
                component.setBlightsOnPos(pos, Set.of(type));
            });
        });
        context.getSource().sendFeedback(() -> Text.literal("Added %s to all BlockPos in %s".formatted(type, box)), true);
        return Command.SINGLE_SUCCESS;
    }
}
