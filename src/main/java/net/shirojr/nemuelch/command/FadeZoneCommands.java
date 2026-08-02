package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.LocationalFadeComponent;
import net.shirojr.nemuelch.compat.cca.util.FadeZone;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FadeZoneCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType ID_ALREADY_EXISTS =
            new SimpleCommandExceptionType(Text.literal("Zone ID already exists"));
    private static final SimpleCommandExceptionType NO_ZONES_PRESENT =
            new SimpleCommandExceptionType(Text.literal("No Fade Zones found"));
    private static final SimpleCommandExceptionType ZONE_NOT_FOUND =
            new SimpleCommandExceptionType(Text.literal("Zone ID not found"));
    private static final SimpleCommandExceptionType WRONG_MIN_MAX =
            new SimpleCommandExceptionType(Text.literal("MIN radius needs to be smaller than MAX radius"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("fadeZones")
                .then(literal("add")
                        .then(literal("new")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .then(argument("center", Vec3ArgumentType.vec3())
                                                .then(argument("minRadius", DoubleArgumentType.doubleArg(0))
                                                        .then(argument("maxRadius", DoubleArgumentType.doubleArg(0))
                                                                .then(argument("inverted", BoolArgumentType.bool())
                                                                        .executes(context -> FadeZoneCommands.addNew(context, Set.of()))
                                                                        .then(argument("targets", EntityArgumentType.players())
                                                                                .executes(context -> FadeZoneCommands.addNew(context, EntityArgumentType.getPlayers(context, "targets")))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("copy")
                                .then(argument("newId", IdentifierArgumentType.identifier())
                                        .then(argument("center", Vec3ArgumentType.vec3())
                                                .then(argument("existingId", IdentifierArgumentType.identifier())
                                                        .executes(FadeZoneCommands::addCopy)
                                                )
                                        )
                                )
                        )
                )
                .then(literal("print")
                        .then(literal("all")
                                .executes(FadeZoneCommands::printAll)
                        )
                        .then(literal("entry")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests((context, builder) -> {
                                            LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
                                            component.getZones().keySet().forEach(identifier -> builder.suggest(identifier.toString()));
                                            return builder.buildFuture();
                                        })
                                        .executes(FadeZoneCommands::print)
                                )
                        )
                )
                .then(literal("clear")
                        .executes(FadeZoneCommands::clearAll)
                        .then(argument("id", IdentifierArgumentType.identifier())
                                .suggests((context, builder) -> {
                                    LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
                                    component.getZones().keySet().forEach(identifier -> builder.suggest(identifier.toString()));
                                    return builder.buildFuture();
                                })
                                .executes(FadeZoneCommands::clear)
                        )
                )
                .build();

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int print(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        LocationalFadeComponent component = LocationalFadeComponent.get(source.getWorld());
        FadeZone zone = component.getZones().get(id);
        if (zone == null) throw ZONE_NOT_FOUND.create();
        source.sendFeedback(() -> Text.empty()
                .append("Selected Zone: ")
                .append(Text.literal("[%s]".formatted(id)).formatted(Formatting.AQUA)), false);
        source.sendFeedback(() -> Text.empty()
                .append(Text.literal("- center: "))
                .append(zone.center().toString()).styled(style -> style
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %s %s %s".formatted(zone.center().x, zone.center().y, zone.center().z)))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to teleport")))
                ), false);
        source.sendFeedback(() -> Text.literal("- min / max distance: %s/%s".formatted(zone.minRadius(), zone.maxRadius())), false);
        source.sendFeedback(() -> Text.literal("- is inverted: %s".formatted(zone.inverted())), false);
        List<Text> targets = new ArrayList<>();
        zone.targets().forEach(uuid -> {
            Entity entity = source.getWorld().getEntity(uuid);
            if (entity == null) targets.add(Text.literal(uuid.toString()));
            else targets.add(entity.getName());
        });
        if (targets.isEmpty()) {
            source.sendFeedback(() -> Text.literal("- targets: ALL"), false);
        } else {
            MutableText output = Text.empty().append("- targets: ");
            for (int i = 0; i < targets.size(); i++) {
                output.append(targets.get(i));
                if (i < targets.size() - 1) {
                    output.append(", ");
                }
            }
            context.getSource().sendFeedback(() -> output, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int printAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
        if (component.size() <= 0) {
            throw NO_ZONES_PRESENT.create();
        }
        context.getSource().sendFeedback(() -> Text.literal("Current Zones:"), false);
        List<Identifier> sortedIds = component.getZones().keySet().stream().sorted().toList();
        MutableText output = Text.empty();
        for (int i = 0; i < sortedIds.size(); i++) {
            Identifier entry = sortedIds.get(i);
            output.append(Text.literal("[%s]".formatted(entry)).styled(style -> style
                    .withColor(Formatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nemuelch fadeZones print entry " + entry))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Show content of Fade Zone")))
            ));
            if (i < sortedIds.size() - 1) {
                output.append(Text.literal(", "));
            }
        }
        context.getSource().sendFeedback(() -> output, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int clear(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        if (!component.remove(id)) {
            throw ZONE_NOT_FOUND.create();
        }
        context.getSource().sendFeedback(() -> Text.literal("Zone [%s] removed".formatted(id.toString())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int clearAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
        if (component.size() <= 0) {
            throw NO_ZONES_PRESENT.create();
        }
        component.removeAll();
        context.getSource().sendFeedback(() -> Text.literal("Removed all Fade Zones from World"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addCopy(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier newId = IdentifierArgumentType.getIdentifier(context, "newId");
        Identifier existingId = IdentifierArgumentType.getIdentifier(context, "existingId");
        LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
        FadeZone copyTarget = component.getZones().get(existingId);
        if (copyTarget == null) {
            throw ZONE_NOT_FOUND.create();
        }
        Vec3d center = Vec3ArgumentType.getVec3(context, "center");
        FadeZone newZone = new FadeZone(newId, center, copyTarget.minRadius(), copyTarget.maxRadius(),
                copyTarget.inverted(), new HashSet<>(copyTarget.targets()));
        if (!component.put(newZone)) {
            throw ID_ALREADY_EXISTS.create();
        }
        context.getSource().sendFeedback(() -> Text.literal("Created new Fade Zone"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addNew(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        LocationalFadeComponent component = LocationalFadeComponent.get(context.getSource().getWorld());
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        Vec3d center = Vec3ArgumentType.getVec3(context, "center");
        double minRadius = DoubleArgumentType.getDouble(context, "minRadius");
        double maxRadius = DoubleArgumentType.getDouble(context, "maxRadius");
        if (minRadius >= maxRadius) {
            throw WRONG_MIN_MAX.create();
        }
        boolean inverted = BoolArgumentType.getBool(context, "inverted");
        HashSet<UUID> targetUuids = new HashSet<>();
        targets.forEach(player -> targetUuids.add(player.getUuid()));
        FadeZone fadeZone = new FadeZone(id, center, minRadius, maxRadius, inverted, targetUuids);
        if (!component.put(fadeZone)) {
            throw ID_ALREADY_EXISTS.create();
        }
        if (targetUuids.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("No Targets specified. Zone will be applied to all players"), false);
        }
        context.getSource().sendFeedback(() -> Text.literal("Created new Fade Zone"), true);
        return Command.SINGLE_SUCCESS;
    }
}
