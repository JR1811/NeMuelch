package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.command.argument.PhaseArgumentType;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.compat.timewind.Phase;
import net.shirojr.nemuelch.compat.timewind.SafeTimeHandler;
import net.shirojr.nemuelch.init.NeMuelchCustomRegistries;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionType;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OccasionCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NOT_FOUND =
            new SimpleCommandExceptionType(Text.literal("OccasionType not found"));
    private static final SimpleCommandExceptionType NO_ACTIVE_ENTRIES =
            new SimpleCommandExceptionType(Text.literal("No entries found"));
    private static final SimpleCommandExceptionType ENTRY_ALREADY_PRESENT =
            new SimpleCommandExceptionType(Text.literal("Entry already present in schedule"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("occasion").requires(source -> source.hasPermissionLevel(2))
                .then(literal("add")
                        .then(argument("occasion", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, NeMuelchCustomRegistries.OCCASIONS_REGISTRY_KEY))
                                .then(argument("duration", LongArgumentType.longArg(0))
                                        .executes(commandContext -> addOccasion(commandContext, commandContext.getSource().getWorld().getTime()))
                                        .then(argument("startTime", LongArgumentType.longArg(0))
                                                .executes(commandContext -> {
                                                    long startTime = LongArgumentType.getLong(commandContext, "startTime");
                                                    return addOccasion(commandContext, startTime);
                                                })
                                        )
                                )
                        )
                )
                .then(literal("clear")
                        .then(literal("all")
                                .executes(OccasionCommands::clearAll)
                        )
                )
                .then(literal("info")
                        .then(literal("all")
                                .executes(context -> getInfo(context, -1))
                        )
                        .then(literal("at")
                                .then(literal("now")
                                        .executes(commandContext -> {
                                            long currentTime = commandContext.getSource().getWorld().getTime();
                                            return getInfo(commandContext, currentTime);
                                        })
                                )
                                .then(argument("during", LongArgumentType.longArg(0))
                                        .executes(commandContext -> {
                                            long currentTime = LongArgumentType.getLong(commandContext, "during");
                                            return getInfo(commandContext, currentTime);
                                        })
                                )
                        )
                )
                .then(literal("utility")
                        .then(literal("currentTime")
                                .executes(context -> getCurrentTime(context, false))
                                .then(argument("copyToClipboard", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean copyToClipboard = BoolArgumentType.getBool(context, "copyToClipboard");
                                            return getCurrentTime(context, copyToClipboard);
                                        })
                                )
                        )
                        .then(literal("length")
                                .then(argument("phase", PhaseArgumentType.phase())
                                        .then(argument("copyToClipboard", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    Phase phase = PhaseArgumentType.getPhase(context, "phase");
                                                    boolean copyToClipboard = BoolArgumentType.getBool(context, "copyToClipboard");
                                                    return getPhaseDuration(context, phase, copyToClipboard);
                                                })
                                        )
                                )
                        )
                        .then(literal("calculate")
                                .then(argument("time", LongArgumentType.longArg(0))
                                        .then(literal("add")
                                                .then(literal("days")
                                                        .then(argument("days", DoubleArgumentType.doubleArg())
                                                                .then(argument("copyToClipboard", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            boolean copyToClipboard = BoolArgumentType.getBool(context, "copyToClipboard");
                                                                            long time = LongArgumentType.getLong(context, "time");
                                                                            double days = DoubleArgumentType.getDouble(context, "days");
                                                                            return utilityCalcAddTime(context, time, days, 0, copyToClipboard);
                                                                        })
                                                                )
                                                        )
                                                )
                                                .then(literal("nights")
                                                        .then(argument("nights", DoubleArgumentType.doubleArg())
                                                                .then(argument("copyToClipboard", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            boolean copyToClipboard = BoolArgumentType.getBool(context, "copyToClipboard");
                                                                            long time = LongArgumentType.getLong(context, "time");
                                                                            double nights = DoubleArgumentType.getDouble(context, "nights");
                                                                            return utilityCalcAddTime(context, time, 0, nights, copyToClipboard);
                                                                        })
                                                                )
                                                        )
                                                )
                                                .then(literal("daysAndNights")
                                                        .then(argument("daysAndNights", DoubleArgumentType.doubleArg())
                                                                .then(argument("copyToClipboard", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            boolean copyToClipboard = BoolArgumentType.getBool(context, "copyToClipboard");
                                                                            long time = LongArgumentType.getLong(context, "time");
                                                                            double daysAndNights = DoubleArgumentType.getDouble(context, "daysAndNights");
                                                                            return utilityCalcAddTime(context, time, daysAndNights, daysAndNights, copyToClipboard);
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                )

                        )
                )
        );
    }

    private int utilityCalcAddTime(CommandContext<ServerCommandSource> context, long time, double days, double nights, boolean copyToClipboard) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        long calcTime = (long) (time + (SafeTimeHandler.getDayDuration(world) * days) + (SafeTimeHandler.getNightDuration(world) * nights));
        source.sendFeedback(() -> Text.literal("Calculated Time: " + calcTime), true);

        ServerPlayerEntity player = source.getPlayer();
        if (player != null && copyToClipboard) {
            source.sendFeedback(() -> Text.literal("Also sent to your client clipboard"), false);
            sendToClientClipboard(List.of(player), Long.toString(calcTime));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getCurrentTime(CommandContext<ServerCommandSource> context, boolean copyToClipboard) {
        ServerCommandSource source = context.getSource();
        long currentTime = source.getWorld().getTimeOfDay();
        source.sendFeedback(() -> Text.literal("Current World Time: " + currentTime), true);

        ServerPlayerEntity player = source.getPlayer();
        if (player != null && copyToClipboard) {
            source.sendFeedback(() -> Text.literal("Also sent to your client clipboard"), false);
            sendToClientClipboard(List.of(player), Long.toString(source.getWorld().getTime()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int getPhaseDuration(CommandContext<ServerCommandSource> context, Phase phase, boolean copyToClipboard) {
        ServerCommandSource source = context.getSource();
        long duration = phase.getDuration(source.getWorld());
        source.sendFeedback(() -> Text.literal(phase.asString() + " length: " + duration), true);

        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null && copyToClipboard) {
            source.sendFeedback(() -> Text.literal("Also sent to your client clipboard"), false);
            sendToClientClipboard(List.of(player), Long.toString(duration));
        }
        return Command.SINGLE_SUCCESS;
    }


    private int getInfo(CommandContext<ServerCommandSource> commandContext, long time) throws CommandSyntaxException {
        ServerCommandSource source = commandContext.getSource();
        ServerWorld world = source.getWorld();
        OccasionsWorldComponent component = OccasionsWorldComponent.get(world);

        List<OccasionEntry> occasions;
        if (time == -1) {
            occasions = component.getUnsyncedScheduledOccasions();
        } else {
            occasions = component.getUnsyncedActiveOccasions(time);
        }

        if (occasions.isEmpty()) {
            throw NO_ACTIVE_ENTRIES.create();
        }
        for (OccasionEntry occasion : occasions) {
            Text header = Text.empty().append(occasion.getType().getName()).append(Text.literal(" - [Current Index: %s]".formatted(component.getIndex(occasion))));
            source.sendFeedback(() -> header, true);
            for (Text description : occasion.getType().getDescription()) {
                source.sendFeedback(() -> description, true);
            }
            source.sendFeedback(() -> Text.literal("Starting at: [%s]".formatted(occasion.getStartTime())), true);
            source.sendFeedback(() -> Text.literal("Duration: [%s]".formatted(occasion.getDuration())), true);
            source.sendFeedback(() -> Text.literal("------------"), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        OccasionsWorldComponent component = OccasionsWorldComponent.get(source.getWorld());
        if (component.isEmpty()) {
            throw NO_ACTIVE_ENTRIES.create();
        }
        component.modifyScheduledOccasions(List::clear, true);
        source.sendFeedback(() -> Text.literal(source.getName() + " cleared all scheduled Occasions for this world"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addOccasion(CommandContext<ServerCommandSource> context, long startTime) throws CommandSyntaxException {
        RegistryKey<OccasionType> occasionKey = RegistryEntryArgumentType.getRegistryEntry(context, "occasion", NeMuelchCustomRegistries.OCCASIONS_REGISTRY_KEY).getKey().orElseThrow(NOT_FOUND::create);
        OccasionType occasionType = NeMuelchCustomRegistries.OCCASIONS.get(occasionKey);
        if (occasionType == null) throw NOT_FOUND.create();
        long duration = LongArgumentType.getLong(context, "duration");

        ServerCommandSource source = context.getSource();
        OccasionsWorldComponent component = OccasionsWorldComponent.get(source.getWorld());
        List<OccasionEntry> removedEntries = component.addOccasion(new OccasionEntry(occasionType, startTime, duration));
        if (removedEntries == null) {
            throw ENTRY_ALREADY_PRESENT.create();
        }

        for (Text descriptionLine : occasionType.getDescription()) {
            source.sendFeedback(() -> descriptionLine, true);
        }
        source.sendFeedback(occasionType::getName, true);
        source.sendFeedback(() -> Text.literal("Start: " + startTime), true);
        source.sendFeedback(() -> Text.literal("Duration: " + duration), true);
        if (!removedEntries.isEmpty()) {
            StringBuilder sb = new StringBuilder("Removed Entries: ");
            for (int i = 0; i < removedEntries.size(); i++) {
                OccasionEntry removedEntry = removedEntries.get(i);
                sb.append(removedEntry.getType().getName().getString()).append(" starting at: ").append(removedEntry.getStartTime());
                if (i < removedEntries.size() - 1) {
                    sb.append(", ");
                }
            }
            source.sendFeedback(() -> Text.literal(sb.toString()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void sendToClientClipboard(Collection<ServerPlayerEntity> targets, String input) {
        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(input);
            ServerPlayNetworking.send(target, NetworkIdentifiers.STRING_TO_CLIENT_CLIPBOARD, buf);
        }
    }
}
