package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.component.RespawnLocationsComponent;
import net.shirojr.nemuelch.compat.cca.util.RespawnLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class RespawnCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType LOCATION_NAME_NOT_USABLE =
            new SimpleCommandExceptionType(Text.literal("Invalid location name | Example: namespace:location_name"));
    private static final SimpleCommandExceptionType LOCATION_ALREADY_PRESENT =
            new SimpleCommandExceptionType(Text.literal("Location already exists"));
    private static final SimpleCommandExceptionType LOCATION_NOT_PRESENT =
            new SimpleCommandExceptionType(Text.literal("Location was not registered"));
    private static final SimpleCommandExceptionType INVALID_COMMAND_SOURCE =
            new SimpleCommandExceptionType(Text.literal("Command only executable by player entities"));

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> respawnCommandNode = literal("respawn")
                .then(literal("location")
                        .then(literal("add")
                                .then(argument("identifier", StringArgumentType.string())
                                        .then(argument("position", Vec3ArgumentType.vec3())
                                                .executes(RespawnCommands::addNewLocation)
                                                .then(argument("dimension", DimensionArgumentType.dimension())
                                                        .executes(RespawnCommands::addNewLocationWithDimension)
                                                )
                                        )
                                )
                        )
                        .then(literal("remove")
                                .executes(RespawnCommands::clearLocations)
                                .then(argument("identifier", StringArgumentType.string())
                                        .executes(RespawnCommands::removeLocation)
                                )
                        )
                        .then(literal("print")
                                .executes(RespawnCommands::printLocations)
                                .then(argument("dimension", DimensionArgumentType.dimension())
                                        .executes(RespawnCommands::printLocationsOfDimension)
                                )
                        )
                )
                .then(literal("entity")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(literal("assign")
                                        .then(argument("locationIdentifier", StringArgumentType.string())
                                                .suggests(RespawnCommands::suggestPossibleLocations)
                                                .executes(RespawnCommands::assignLocationToPlayers)
                                        )
                                )
                                .then(literal("print")
                                        .executes(RespawnCommands::printAssignedLocationsOfPlayer)
                                )
                                .then(literal("remove")
                                        .then(argument("locationIdentifier", StringArgumentType.string())
                                                .suggests(RespawnCommands::suggestPossibleLocations)
                                                .executes(RespawnCommands::removeLocationFromPlayers)
                                        )
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(respawnCommandNode.build());
    }

    // region Suggestions
    private static CompletableFuture<Suggestions> suggestPossibleLocations(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        RespawnLocationsComponent respawnLocationsComponent = RespawnLocationsComponent.get(context.getSource().getWorld());
        for (Identifier identifier : respawnLocationsComponent.getLocations().keySet()) {
            String entry = identifier.toString();
            if (!entry.contains(input)) continue;
            builder.suggest("\"%s\"".formatted(entry));
        }
        return builder.buildFuture();
    }
    // endregion

    // region Location Commands
    private static int addNewLocation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = formatAsIdentifier(StringArgumentType.getString(context, "identifier"));
        if (identifier == null) {
            throw LOCATION_NAME_NOT_USABLE.create();
        }
        BlockPos position = BlockPosArgumentType.getBlockPos(context, "position");
        addNewLocation(context.getSource().getWorld(), identifier, position, null);
        context.getSource().sendFeedback(() -> Text.literal("Added %s as a respawn location at %s"
                .formatted(identifier.toString(), position)), true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int addNewLocationWithDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = formatAsIdentifier(StringArgumentType.getString(context, "identifier"));
        if (identifier == null) {
            throw LOCATION_NAME_NOT_USABLE.create();
        }
        BlockPos position = BlockPosArgumentType.getBlockPos(context, "position");
        ServerWorld dimension = DimensionArgumentType.getDimensionArgument(context, "dimension");
        addNewLocation(context.getSource().getWorld(), identifier, position, dimension.getRegistryKey());
        context.getSource().sendFeedback(() -> Text.literal("Added %s as a respawn location at %s in %s"
                .formatted(identifier.toString(), position, dimension.getRegistryKey())), true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int clearLocations(CommandContext<ServerCommandSource> context) {
        removeLocations(context.getSource().getWorld(), null);
        context.getSource().sendFeedback(() -> Text.literal("Cleared all respawn locations and their assigned players"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeLocation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier identifier = formatAsIdentifier(StringArgumentType.getString(context, "identifier"));
        if (identifier == null) {
            throw LOCATION_NAME_NOT_USABLE.create();
        }
        if (!RespawnLocationsComponent.get(context.getSource().getWorld()).getLocations().containsKey(identifier)) {
            throw LOCATION_NOT_PRESENT.create();
        }
        removeLocations(context.getSource().getWorld(), identifier);
        context.getSource().sendFeedback(() -> Text.literal("Removed %s and its assigned players".formatted(identifier.toString())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int printLocations(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw INVALID_COMMAND_SOURCE.create();
        }
        printLocations(context.getSource().getWorld(), null, player);
        return Command.SINGLE_SUCCESS;
    }

    private static int printLocationsOfDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw INVALID_COMMAND_SOURCE.create();
        }
        ServerWorld dimension = DimensionArgumentType.getDimensionArgument(context, "dimension");
        printLocations(context.getSource().getWorld(), dimension.getRegistryKey(), player);
        return Command.SINGLE_SUCCESS;
    }
    // endregion

    // region Entity Commands
    private static int assignLocationToPlayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targetList = EntityArgumentType.getPlayers(context, "targets");
        Identifier identifier = formatAsIdentifier(StringArgumentType.getString(context, "locationIdentifier"));
        if (identifier == null) {
            throw LOCATION_NAME_NOT_USABLE.create();
        }
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(context.getSource().getWorld());
        if (!respawnComponent.getLocations().containsKey(identifier)) {
            throw LOCATION_NOT_PRESENT.create();
        }
        for (ServerPlayerEntity serverPlayerEntity : targetList) {
            respawnComponent.assign(identifier, serverPlayerEntity.getUuid());
            context.getSource().sendFeedback(() -> Text.literal("Assigned %s to ".formatted(identifier.toString())).append(serverPlayerEntity.getDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeLocationFromPlayers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targetList = EntityArgumentType.getPlayers(context, "targets");
        Identifier identifier = formatAsIdentifier(StringArgumentType.getString(context, "locationIdentifier"));
        if (identifier == null) {
            throw LOCATION_NAME_NOT_USABLE.create();
        }
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(context.getSource().getWorld());
        if (!respawnComponent.getLocations().containsKey(identifier)) {
            throw LOCATION_NOT_PRESENT.create();
        }
        for (ServerPlayerEntity serverPlayerEntity : targetList) {
            respawnComponent.unassign(identifier, serverPlayerEntity.getUuid());
            context.getSource().sendFeedback(() -> Text.literal("Removed %s from ".formatted(identifier.toString())).append(serverPlayerEntity.getDisplayName()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int printAssignedLocationsOfPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw INVALID_COMMAND_SOURCE.create();
        }
        for (ServerPlayerEntity target : EntityArgumentType.getPlayers(context, "targets")) {
            RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(context.getSource().getWorld());
            player.sendMessage(Text.literal("Possible Respawn Locations for ").append(target.getDisplayName()).append(":"));
            StringBuilder locationsStringBuilder = new StringBuilder();
            for (RespawnLocation respawnLocation : respawnComponent.getAssigned(target.getUuid())) {
                if (!locationsStringBuilder.isEmpty()) {
                    locationsStringBuilder.append(", ");
                }
                locationsStringBuilder.append(respawnLocation.identifier().toString());
            }
            player.sendMessage(Text.literal(locationsStringBuilder.toString()));
        }
        return Command.SINGLE_SUCCESS;
    }
    // endregion

    // region Utility
    private static Identifier formatAsIdentifier(String name) {
        if (!name.contains(":")) {
            name = NeMuelch.MOD_ID + ":" + name;
        }
        name = name.replaceAll("([A-Z])", "_$1").replaceAll("[./]", "_");
        if (name.charAt(0) == '_') {
            name = name.substring(1);
        }
        name = name.toLowerCase(Locale.ROOT);
        return Identifier.tryParse(name);
    }

    private static void addNewLocation(ServerWorld world, Identifier identifier, BlockPos position, @Nullable RegistryKey<World> dimension) throws CommandSyntaxException {
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(world);
        if (respawnComponent.getLocations().containsKey(identifier)) {
            throw LOCATION_ALREADY_PRESENT.create();
        }
        if (dimension == null) {
            dimension = world.getRegistryKey();
        }
        respawnComponent.add(new RespawnLocation(identifier, position, dimension));
    }

    private static void removeLocations(ServerWorld world, @Nullable Identifier identifier) {
        RespawnLocationsComponent respawnComponent = RespawnLocationsComponent.get(world);
        if (identifier == null) {
            respawnComponent.remove();
            return;
        }
        respawnComponent.remove(List.of(identifier));
    }

    private static void printLocations(ServerWorld world, @Nullable RegistryKey<World> targetDimension, ServerPlayerEntity target) {
        Map<Identifier, RespawnLocation> allLocations = RespawnLocationsComponent.get(world).getLocations();
        List<RespawnLocation> filteredLocations = new ArrayList<>();
        if (targetDimension == null) {
            filteredLocations.addAll(allLocations.values());
        } else {
            for (RespawnLocation entry : allLocations.values()) {
                if (!entry.dimension().equals(targetDimension)) continue;
                filteredLocations.add(entry);
            }
        }

        target.sendMessage(Text.literal("Available Respawn Locations:"));
        for (RespawnLocation entry : filteredLocations) {
            MutableText result = Text.literal(entry.identifier().toString());
            BlockPos pos = entry.position();
            result.append(" | X:%s Y:%s Z:%s | %s".formatted(pos.getX(), pos.getY(), pos.getZ(), entry.dimension().getValue().getPath()));
            target.sendMessage(result);
        }
    }
    // endregion
}
