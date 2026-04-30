package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.compat.cca.implementation.RopesComponent;
import net.shirojr.nemuelch.compat.cca.util.RopeData;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RopeCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_ROPE_FOUND =
            new SimpleCommandExceptionType(Text.literal("No Rope found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("ropes")
                .then(literal("add")
                        .then(argument("posA", BlockPosArgumentType.blockPos())
                                .then(argument("posB", BlockPosArgumentType.blockPos())
                                        .executes(RopeCommands::createRope)
                                )
                        )
                )
                .then(literal("remove")
                        .then(argument("posA", BlockPosArgumentType.blockPos())
                                .executes(context -> RopeCommands.removeRope(
                                                context, BlockPosArgumentType.getBlockPos(context, "posA"),
                                                null
                                        )
                                )
                                .then(argument("posB", BlockPosArgumentType.blockPos())
                                        .executes(context -> RopeCommands.removeRope(
                                                        context,
                                                        BlockPosArgumentType.getBlockPos(context, "posA"),
                                                        BlockPosArgumentType.getBlockPos(context, "posB")
                                                )
                                        )
                                )
                        )
                )
                .then(literal("print")
                        .executes(RopeCommands::printRopes)
                );
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int printRopes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);
        List<RopeData> ropes = ropesComponent.getRopes();
        if (ropes.isEmpty()) throw NO_ROPE_FOUND.create();
        source.sendFeedback(() -> Text.literal("Rope entries: "), true);
        for (int i = 0; i < ropes.size(); i++) {
            RopeData ropeData = ropes.get(i);
            int index = i;
            source.sendFeedback(() -> {
                        MutableText output = Text.empty();
                        output.append(Text.literal("Entry %s: ".formatted(index)));
                        BlockPos pointA = ropeData.pointA();
                        output.append(Text.literal("[%s]".formatted(pointA.toShortString()))
                                .formatted(Formatting.GREEN).styled(style -> style
                                        .withClickEvent(
                                                new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND, "/tp %s %s %s".formatted(
                                                        pointA.getX(), pointA.getY(), pointA.getZ()
                                                ))
                                        )
                                        .withHoverEvent(
                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport"))
                                        )
                                )
                        );
                        output.append(Text.literal(" - "));
                        BlockPos pointB = ropeData.pointB();
                        output.append(Text.literal("[%s]".formatted(pointB.toShortString()))
                                .formatted(Formatting.GREEN).styled(style -> style
                                        .withClickEvent(
                                                new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND, "/tp %s %s %s".formatted(
                                                        pointB.getX(), pointB.getY(), pointB.getZ()
                                                ))
                                        )
                                        .withHoverEvent(
                                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport"))
                                        )
                                )
                        );
                        return output;
                    },
                    true
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeRope(CommandContext<ServerCommandSource> context, BlockPos posA, @Nullable BlockPos posB) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);

        HashSet<RopeData> removeEntries = new HashSet<>();
        for (RopeData rope : ropesComponent.getRopes()) {
            if (posB == null) {
                if (rope.contains(posA)) {
                    removeEntries.add(rope);
                }
            } else {
                if (rope.contains(posA, posB)) {
                    removeEntries.add(rope);
                }
            }
        }
        if (removeEntries.isEmpty()) {
            throw NO_ROPE_FOUND.create();
        }
        ropesComponent.modifyRopes(true, ropeData -> ropeData.removeAll(removeEntries));
        context.getSource().sendFeedback(() -> Text.literal("Removed Entries"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int createRope(CommandContext<ServerCommandSource> context) {
        BlockPos posA = BlockPosArgumentType.getBlockPos(context, "posA");
        BlockPos posB = BlockPosArgumentType.getBlockPos(context, "posB");
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);
        ropesComponent.modifyRopes(true, ropeData -> ropeData.add(new RopeData(posA, posB)));
        context.getSource().sendFeedback(() ->
                        Text.literal("Added Rope between [%s] and [%s]".formatted(posA.toShortString(), posB.toShortString())),
                true
        );
        return Command.SINGLE_SUCCESS;
    }
}
