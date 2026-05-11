package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.FleetingNotesComponent;
import net.shirojr.nemuelch.compat.cca.util.FleetingNoteData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FleetingNotesCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_ENTRIES =
            new SimpleCommandExceptionType(Text.literal("No Entries"));
    private static final SimpleCommandExceptionType NO_SUCH_ENTRY =
            new SimpleCommandExceptionType(Text.literal("No such Entry, use the \"print\" command for a valid index"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("fleetingNote")
                .then(literal("create")
                        .then(argument("time", IntegerArgumentType.integer(1))
                                .then(argument("distance", FloatArgumentType.floatArg(0))
                                        .then(argument("visibleAngle", FloatArgumentType.floatArg(1))
                                                .suggests((context, builder) -> builder.suggest(20).buildFuture())
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .then(argument("lines", StringArgumentType.string())
                                                                .suggests((context, builder) -> {
                                                                    builder.suggest("\"Example Line 1 | Example Line 2 | Example Line 3\"");
                                                                    return builder.buildFuture();
                                                                })
                                                                .executes(FleetingNotesCommand::create)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("print")
                        .executes(FleetingNotesCommand::print)
                )
                .then(literal("clear")
                        .executes(context -> FleetingNotesCommand.clear(context, null))
                        .then(argument("index", IntegerArgumentType.integer(0))
                                .executes(context ->
                                        FleetingNotesCommand.clear(context, IntegerArgumentType.getInteger(context, "index"))
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int print(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        FleetingNotesComponent component = FleetingNotesComponent.get(source.getWorld());
        List<FleetingNoteData.Positioned> entries = component.getUnsyncedData();
        if (entries.isEmpty()) {
            throw NO_ENTRIES.create();
        }
        for (int i = 0; i < entries.size(); i++) {
            if (i == 0) {
                source.sendFeedback(Text::empty, true);
            }
            FleetingNoteData.Positioned entry = entries.get(i);
            MutableText output = Text.empty();
            Vec3d pos = entry.pos();
            output.append(Text.literal("Entry [%s] at (%s %s %s):".formatted(i, pos.x, pos.y, pos.z)).formatted(Formatting.WHITE));
            source.sendFeedback(() -> output, true);
            for (Text line : entry.data().getLines()) {
                source.sendFeedback(() -> line.copy().formatted(Formatting.GRAY), true);
            }
            if (i <= entries.size() - 1) {
                source.sendFeedback(Text::empty, true);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandContext<ServerCommandSource> context, @Nullable Integer index) throws CommandSyntaxException {
        FleetingNotesComponent component = FleetingNotesComponent.get(context.getSource().getWorld());
        if (component.isEmpty()) {
            throw NO_ENTRIES.create();
        }
        if (index == null) {
            component.modifyData(true, List::clear);
            context.getSource().sendFeedback(() -> Text.literal("Cleared Entries"), true);
        } else {
            List<FleetingNoteData.Positioned> data = component.getUnsyncedData();
            if (index >= data.size()) {
                throw NO_SUCH_ENTRY.create();
            }
            component.modifyData(true, entries -> entries.remove(index.intValue()));
            context.getSource().sendFeedback(() -> Text.literal("Cleared Entry [%s]".formatted(index)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        int time = IntegerArgumentType.getInteger(context, "time");
        float distance = FloatArgumentType.getFloat(context, "distance");
        float angle = FloatArgumentType.getFloat(context, "visibleAngle");
        Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
        String[] lines = StringArgumentType.getString(context, "lines").split("\\|");

        List<Text> texts = new ArrayList<>();
        for (String line : lines) {
            texts.add(Text.literal(line.trim()));
        }
        FleetingNotesComponent component = FleetingNotesComponent.get(context.getSource().getWorld());
        component.modifyData(
                true,
                entries -> entries.add(new FleetingNoteData.Positioned(pos, new FleetingNoteData(time, distance, angle, texts)))
        );
        context.getSource().sendFeedback(() -> Text.literal("Added new Fleeting Note to world"), true);
        return Command.SINGLE_SUCCESS;
    }
}
