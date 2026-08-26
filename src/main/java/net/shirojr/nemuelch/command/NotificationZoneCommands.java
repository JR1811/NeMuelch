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
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.NotificationZone;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NotificationZoneCommands implements CommandRegistrationCallback {
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
        NotificationZoneComponent component = NotificationZoneComponent.get(world);
        component.getRegistered().forEach(key -> builder.suggest(key.toString()));
        return builder.buildFuture();
    };

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("notificationZone").requires(source -> source.hasPermissionLevel(2))
                .then(literal("zone")
                        .then(literal("register")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .executes(NotificationZoneCommands::registerZone)
                                )
                        )
                        .then(literal("unregister")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                        .executes(NotificationZoneCommands::unregisterZone)
                                )
                        )
                        .then(literal("listener")
                                .then(literal("add")
                                        .then(argument("id", IdentifierArgumentType.identifier())
                                                .suggests(REGISTERED_ZONES_SUGGESTER)
                                                .then(argument("listeners", EntityArgumentType.players())
                                                        .executes(context ->
                                                                NotificationZoneCommands.addListeners(context, null)
                                                        )
                                                        .then(argument("notificationSound", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.SOUND_EVENT))
                                                                .executes(context ->
                                                                        NotificationZoneCommands.addListeners(context, RegistryEntryArgumentType.getRegistryEntry(context, "notificationSound", RegistryKeys.SOUND_EVENT))
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(literal("remove")
                                        .then(argument("id", IdentifierArgumentType.identifier())
                                                .suggests(REGISTERED_ZONES_SUGGESTER)
                                                .then(argument("listeners", EntityArgumentType.players())
                                                        .executes(NotificationZoneCommands::removeListeners)
                                                )
                                        )
                                )
                                .then(literal("notificationSound")
                                        .then(literal("set")
                                                .then(argument("id", IdentifierArgumentType.identifier())
                                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                                        .then(argument("listeners", EntityArgumentType.players())
                                                                .then(argument("notificationSound", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.SOUND_EVENT))
                                                                        .executes(context ->
                                                                                NotificationZoneCommands.setNotificationSound(context, RegistryEntryArgumentType.getRegistryEntry(context, "notificationSound", RegistryKeys.SOUND_EVENT))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(literal("clear")
                                                .then(argument("id", IdentifierArgumentType.identifier())
                                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                                        .then(argument("listeners", EntityArgumentType.players())
                                                                .executes(NotificationZoneCommands::clearNotificationSound)
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("vertices")
                                .then(argument("id", IdentifierArgumentType.identifier())
                                        .suggests(REGISTERED_ZONES_SUGGESTER)
                                        .then(literal("add")
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(NotificationZoneCommands::addVertex)
                                                )
                                        )
                                        .then(literal("list")
                                                .executes(context ->
                                                        NotificationZoneCommands.listVertices(context, 20)
                                                )
                                                .then(argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(context ->
                                                                NotificationZoneCommands.listVertices(context, IntegerArgumentType.getInteger(context, "amount"))
                                                        )
                                                )
                                        )
                                        .then(literal("remove")
                                                .then(literal("pos")
                                                        .then(argument("pos", Vec3ArgumentType.vec3())
                                                                .executes(NotificationZoneCommands::removeVertexByPos)
                                                        )
                                                )
                                                .then(literal("index")
                                                        .then(argument("index", IntegerArgumentType.integer(0))
                                                                .executes(NotificationZoneCommands::removeVertexByIndex)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build();
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int setNotificationSound(CommandContext<ServerCommandSource> context,
                                            RegistryEntry.Reference<SoundEvent> soundReference) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        NotificationZone zone = component.getZone(id);
        if (zone == null) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        SoundEvent sound = soundReference.value();
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");
        for (ServerPlayerEntity listener : listeners) {
            if (component.modifyNotificationSound(id, listener.getUuid(), sound)) {
                context.getSource().sendFeedback(() ->
                                Text.literal("Set notification sound for %s on %s zone to %s"
                                        .formatted(listener.getName().getString(), id.toString(), sound.getId().toString())),
                        true
                );
            } else {
                context.getSource().sendFeedback(() ->
                                Text.literal(listener.getName().getString() + " is not listening to zone " + id.toString()),
                        true
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearNotificationSound(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        NotificationZone zone = component.getZone(id);
        if (zone == null) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");
        for (ServerPlayerEntity listener : listeners) {
            if (component.modifyNotificationSound(id, listener.getUuid(), null)) {
                context.getSource().sendFeedback(() ->
                                Text.literal("Removed notification sound for %s on %s zone"
                                        .formatted(listener.getName().getString(), id.toString())),
                        true
                );
            } else {
                context.getSource().sendFeedback(() ->
                                Text.literal(listener.getName().getString() + " is not listening to zone " + id.toString()),
                        true
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeVertexByIndex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        NotificationZone zone = component.getZone(id);
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
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        NotificationZone zone = component.getZone(id);
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
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        NotificationZone zone = component.getZone(id);
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

    private static int removeListeners(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        if (!component.containsKey(id)) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");
        for (ServerPlayerEntity listener : listeners) {
            if (component.removeZoneListener(id, listener.getUuid())) {
                context.getSource().sendFeedback(() ->
                                Text.literal("Removed %s from %s zone".formatted(listener.getName().getString(), id.toString())),
                        true
                );
            } else {
                context.getSource().sendFeedback(() ->
                                Text.literal("%s was not registered for %s zone".formatted(listener.getName().getString(), id.toString())),
                        true
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addListeners(CommandContext<ServerCommandSource> context,
                                    @Nullable RegistryEntry.Reference<SoundEvent> soundReference) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        if (!component.containsKey(id)) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        SoundEvent sound = soundReference == null ? null : soundReference.value();
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");
        for (ServerPlayerEntity listener : listeners) {
            if (component.addZoneListener(id, listener.getUuid(), sound)) {
                context.getSource().sendFeedback(() ->
                                Text.literal("Registered %s to %s zone".formatted(listener.getName().getString(), id.toString())),
                        true
                );
            } else {
                context.getSource().sendFeedback(() ->
                                Text.literal("%s was already present for %s zone".formatted(listener.getName().getString(), id.toString())),
                        true
                );
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addVertex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
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
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        if (!component.containsKey(id) || !component.removeZone(id)) {
            throw NO_SUCH_ZONE_ENTRY.create();
        }
        context.getSource().sendFeedback(() ->
                        Text.literal("Removed Notification Zone: " + id.toString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int registerZone(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
        NotificationZoneComponent component = NotificationZoneComponent.get(context.getSource().getWorld());
        if (component.containsKey(id)) {
            throw ZONE_ENTRY_ALREADY_REGISTERED.create();
        }
        component.createZone(id);
        context.getSource().sendFeedback(() ->
                        Text.literal("Created Notification Zone: " + id.toString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }
}
