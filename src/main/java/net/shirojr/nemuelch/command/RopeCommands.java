package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
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
                        .then(argument("posA", Vec3ArgumentType.vec3())
                                .then(argument("posB", Vec3ArgumentType.vec3())
                                        .executes(context -> RopeCommands.createRope(
                                                context,
                                                Vec3ArgumentType.getVec3(context, "posA"),
                                                Vec3ArgumentType.getVec3(context, "posB"),
                                                null, null, null)
                                        )
                                        .then(argument("segments", IntegerArgumentType.integer(1))
                                                .suggests((context, builder) ->
                                                        builder.suggest(20, Text.literal("Increase for longer or slacking ropes")).buildFuture()
                                                )
                                                .then(argument("width", FloatArgumentType.floatArg())
                                                        .suggests((context, builder) ->
                                                                builder.suggest("0.025", Text.literal("A thin rope value")).buildFuture()
                                                        )
                                                        .then(argument("slack", FloatArgumentType.floatArg())
                                                                .suggests((context, builder) ->
                                                                        builder.suggest("3.5", Text.literal("A somewhat slacking rope")).buildFuture()
                                                                )
                                                                .executes(context -> RopeCommands.createRope(
                                                                        context,
                                                                        Vec3ArgumentType.getVec3(context, "posA"),
                                                                        Vec3ArgumentType.getVec3(context, "posB"),
                                                                        IntegerArgumentType.getInteger(context, "segments"),
                                                                        FloatArgumentType.getFloat(context, "width"),
                                                                        FloatArgumentType.getFloat(context, "slack"))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("remove")
                        .then(literal("all")
                                .executes(RopeCommands::removeAllRopes)
                        )
                        .then(literal("entry")
                                .then(literal("byIndex")
                                        .then(argument("index", IntegerArgumentType.integer(0))
                                                .executes(context -> RopeCommands.removeRopeByIndex(context, IntegerArgumentType.getInteger(context, "index")))
                                        )
                                )
                                .then(literal("byPos")
                                        .then(argument("posA", Vec3ArgumentType.vec3())
                                                .executes(context -> RopeCommands.removeRopeByPos(
                                                                context, Vec3ArgumentType.getPosArgument(context, "posA").toAbsolutePos(context.getSource()),
                                                                null
                                                        )
                                                )
                                                .then(argument("posB", Vec3ArgumentType.vec3())
                                                        .executes(context -> RopeCommands.removeRopeByPos(
                                                                        context,
                                                                        Vec3ArgumentType.getPosArgument(context, "posA").toAbsolutePos(context.getSource()),
                                                                        Vec3ArgumentType.getPosArgument(context, "posB").toAbsolutePos(context.getSource())
                                                                )
                                                        )
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
                        Vec3d pointA = ropeData.pointA();
                        output.append(Text.literal("[%s]".formatted(pointA))
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
                        Vec3d pointB = ropeData.pointB();
                        output.append(Text.literal("[%s]".formatted(pointB))
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

    private static int removeRopeByIndex(CommandContext<ServerCommandSource> context, int index) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);
        List<RopeData> ropes = ropesComponent.getRopes();
        if (index > ropes.size() - 1) {
            throw NO_ROPE_FOUND.create();
        }
        ropesComponent.modifyRopes(true, ropeData -> ropeData.remove(index));
        context.getSource().sendFeedback(() -> Text.literal("Removed rope entry [%s]".formatted(index)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeRopeByPos(CommandContext<ServerCommandSource> context, @Nullable Vec3d posA, @Nullable Vec3d posB) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);

        HashSet<RopeData> removeEntries = new HashSet<>();
        boolean removeAll = posA == null && posB == null;
        if (removeAll) {
            removeEntries.addAll(ropesComponent.getRopes());
        } else {
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
        }
        if (removeEntries.isEmpty()) {
            throw NO_ROPE_FOUND.create();
        }
        ropesComponent.modifyRopes(true, ropeData -> ropeData.removeAll(removeEntries));
        String feedback = removeAll ? "Removed all entries" : "Removed entries";
        context.getSource().sendFeedback(() -> Text.literal(feedback), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeAllRopes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);
        if (ropesComponent.isEmpty()) {
            throw NO_ROPE_FOUND.create();
        }
        ropesComponent.modifyRopes(true, List::clear);
        context.getSource().sendFeedback(() -> Text.literal("Removed all Ropes"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int createRope(CommandContext<ServerCommandSource> context, Vec3d posA, Vec3d posB,
                                  @Nullable Integer segments, @Nullable Float width, @Nullable Float slack) {
        ServerWorld world = context.getSource().getWorld();
        RopesComponent ropesComponent = RopesComponent.get(world);
        ropesComponent.modifyRopes(true, ropeData -> {
            if (segments == null || width == null || slack == null) {
                ropeData.add(new RopeData(posA, posB));
            } else {
                ropeData.add(new RopeData(posA, posB, segments, width, slack));
            }
        });
        context.getSource().sendFeedback(() ->
                        Text.literal("Added Rope between [%s] and [%s]".formatted(posA, posB)),
                true
        );
        return Command.SINGLE_SUCCESS;
    }
}
