package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.ClimbingPreventionZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.ComplexZone;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ClimbingPreventionZoneCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_SUCH_ZONE_ENTRY =
            new SimpleCommandExceptionType(Text.literal("No such zone entry found"));
    private static final SimpleCommandExceptionType NO_SUCH_VERTEX_ENTRY =
            new SimpleCommandExceptionType(Text.literal("No such Vertex entry in zone found"));
    private static final SimpleCommandExceptionType OUT_OF_BOUNDS_VERTEX =
            new SimpleCommandExceptionType(Text.literal("Index was out of bound of selected zone vertex amount"));
    private static final SimpleCommandExceptionType ZONE_ENTRY_ALREADY_REGISTERED =
            new SimpleCommandExceptionType(Text.literal("Zone entry already registered"));
    private static final SimpleCommandExceptionType NO_VERTICES =
            new SimpleCommandExceptionType(Text.literal("No vertices present"));

    private static final SuggestionProvider<ServerCommandSource> REGISTERED_ZONES_SUGGESTER = (context, builder) -> {
        ServerWorld world = context.getSource().getWorld();
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(world);
        component.getRegistered().forEach(key -> builder.suggest(key.toString()));
        return builder.buildFuture();
    };

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("climbingZones").requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                .then(literal("prevention")
                        .then(literal("register")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .executes(ClimbingPreventionZoneCommands::registerZone)
                                )
                        )
                        .then(literal("unregister")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                        .executes(ClimbingPreventionZoneCommands::unregisterZone)
                                )
                        )
                        .then(literal("vertices")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                        .then(literal("add")
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(ClimbingPreventionZoneCommands::addVertex)
                                                )
                                        )
                                        .then(literal("list")
                                                .executes(context ->
                                                        ClimbingPreventionZoneCommands.listVertices(context, 20)
                                                )
                                                .then(argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(context ->
                                                                ClimbingPreventionZoneCommands.listVertices(context, IntegerArgumentType.getInteger(context, "amount"))
                                                        )
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(literal("pos")
                                                        .then(argument("pos", Vec3ArgumentType.vec3())
                                                                .executes(ClimbingPreventionZoneCommands::removeVertexByPos)
                                                        )
                                                )
                                                .then(literal("index")
                                                        .then(argument("index", IntegerArgumentType.integer(0))
                                                                .executes(ClimbingPreventionZoneCommands::removeVertexByIndex)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int removeVertexByIndex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        ComplexZone zone = component.getZone(id);
        if (zone == null) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        int index = IntegerArgumentType.getInteger(context, "index");
        List<Vec3d> vertices = zone.getVertices();
        if (index >= vertices.size()) throw OUT_OF_BOUNDS_VERTEX.create();
        Vec3d selectedEntry = vertices.get(index);
        zone.modifyVertices(entries -> entries.remove(selectedEntry));
        context.getSource().sendFeedback(() -> Text.literal("Removed Vertex entry from " + id.toString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeVertexByPos(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        ComplexZone zone = component.getZone(id);
        if (zone == null) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
        if (!component.zoneContainsVertex(id, pos)) throw NO_SUCH_VERTEX_ENTRY.create();
        zone.modifyVertices(vertices -> vertices.remove(pos));
        context.getSource().sendFeedback(() -> Text.literal("Removed Vertex entry from " + id.toString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int listVertices(CommandContext<ServerCommandSource> context, int amount) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        ComplexZone zone = component.getZone(id);
        if (zone == null) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        List<Vec3d> vertices = zone.getVertices();
        if (vertices.isEmpty()) {
            throw NO_VERTICES.create();
        }
        for (int i = 0; i < Math.min(vertices.size(), amount); i++) {
            String index = String.valueOf(i);
            Vec3d vertex = vertices.get(i);
            MutableText line = Text.empty()
                    .append(Text.literal(i + ". - ")
                            .styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, index))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Copy Index to clipboard")))
                            )
                    )
                    .append(
                            Text.literal("[%s %s %s]".formatted(
                                            String.format("%,.3f", vertex.x),
                                            String.format("%,.3f", vertex.y),
                                            String.format("%,.3f", vertex.z)))
                                    .styled(style -> style
                                            .withColor(Formatting.GOLD)
                                            .withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                                                            "%s %s %s".formatted(vertex.x, vertex.y, vertex.z)
                                                    )
                                            )
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            Text.literal("Copy Vec3d to clipboard")
                                                    )
                                            )
                                    )
                    );
            context.getSource().sendFeedback(() -> line, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addVertex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        if (!component.containsKey(id)) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
        component.modifyZoneVertices(id, vertices -> vertices.add(pos));
        context.getSource().sendFeedback(() ->
                        Text.literal("Added [%s] Vertex to %s Zone".formatted(pos, id.toString())),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int unregisterZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        if (!component.containsKey(id) || !component.removeZone(id)) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        context.getSource().sendFeedback(() ->
                        Text.literal("Removed Climbing Prevention Zone: " + id.toString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int registerZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        ClimbingPreventionZoneComponent component = ClimbingPreventionZoneComponent.get(context.getSource().getWorld());
        if (component.containsKey(id)) {
            throw ZONE_ENTRY_ALREADY_REGISTERED.create();
        }
        component.createZone(id);
        context.getSource().sendFeedback(() ->
                        Text.literal("Created Climbing Prevention Zone: " + id.toString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }
}
