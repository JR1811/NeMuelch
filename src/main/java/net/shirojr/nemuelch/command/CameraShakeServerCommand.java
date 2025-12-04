package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.camera.Displacement;
import net.shirojr.nemuelch.camera.DisplacementSequence;
import net.shirojr.nemuelch.camera.Easing;
import net.shirojr.nemuelch.command.argument.EasingArgumentType;
import net.shirojr.nemuelch.compat.cca.implementation.DisplacementSequenceRegistryComponent;
import net.shirojr.nemuelch.item.custom.adminToolItem.CameraDisplacementToolItem;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CameraShakeServerCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_USER =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by an Entity"));
    private static final SimpleCommandExceptionType DUPLICATE_ID =
            new SimpleCommandExceptionType(Text.literal("Sequence with this Key already exists"));
    private static final SimpleCommandExceptionType MISSING_ID =
            new SimpleCommandExceptionType(Text.literal("Sequence with this Key does not exist"));

    private static final SuggestionProvider<ServerCommandSource> DISPLACEMENT_SEQUENCE_SUGGESTER = (context, builder) -> {
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(context.getSource().getServer().getScoreboard());
        return CommandSource.suggestIdentifiers(
                component.getEntryKeys(), builder
        );
    };

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> shakeSubCommand = literal("shake").requires(source -> source.hasPermissionLevel(2))
                .then(literal("item")
                        .then(argument("identifier", IdentifierArgumentType.identifier())
                                .suggests(DISPLACEMENT_SEQUENCE_SUGGESTER)
                                .executes(context -> CameraShakeServerCommand.createItem(context, false, false, false, false))
                                .then(argument("maxRange", DoubleArgumentType.doubleArg())
                                        .executes(context -> CameraShakeServerCommand.createItem(context, true, false, false, false))
                                        .then(literal("origin")
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(context -> CameraShakeServerCommand.createItem(context, true, true, false, false))
                                                        .then(argument("falloffStartDistance", DoubleArgumentType.doubleArg())
                                                                .executes(context -> CameraShakeServerCommand.createItem(context, true, true, false, true))
                                                        )
                                                )
                                                .then(argument("entity", EntityArgumentType.entity())
                                                        .executes(context -> CameraShakeServerCommand.createItem(context, true, false, true, false))
                                                        .then(argument("falloffStartDistance", DoubleArgumentType.doubleArg())
                                                                .executes(context -> CameraShakeServerCommand.createItem(context, true, false, true, true))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("apply")
                        .then(argument("targets", EntityArgumentType.players())
                                .then(literal("startSequence")
                                        .then(argument("identifier", IdentifierArgumentType.identifier())
                                                .suggests(DISPLACEMENT_SEQUENCE_SUGGESTER)
                                                .executes(CameraShakeServerCommand::startDisplacementSequence)
                                        )
                                )
                                .then(literal("stopSequence")
                                        .executes(CameraShakeServerCommand::stopAllDisplacementSequences)
                                        .then(argument("identifier", IdentifierArgumentType.identifier())
                                                .suggests(DISPLACEMENT_SEQUENCE_SUGGESTER)
                                                .executes(CameraShakeServerCommand::stopDisplacementSequence)
                                        )
                                )
                        )
                )
                .then(literal("sequence")
                        .then(literal("register")
                                .then(argument("identifier", IdentifierArgumentType.identifier())
                                        .executes(CameraShakeServerCommand::registerSequence)
                                )
                        )
                        .then(literal("delete")
                                .then(argument("identifier", IdentifierArgumentType.identifier())
                                        .suggests(DISPLACEMENT_SEQUENCE_SUGGESTER)
                                        .executes(CameraShakeServerCommand::removeSequence)
                                )
                                .then(literal("all")
                                        .executes(CameraShakeServerCommand::removeAllSequences)
                                )
                        )
                        .then(literal("edit")
                                .then(argument("identifier", IdentifierArgumentType.identifier())
                                        .suggests(DISPLACEMENT_SEQUENCE_SUGGESTER)
                                        .then(literal("addDisplacement")
                                                .then(argument("duration", IntegerArgumentType.integer(0))
                                                        .then(argument("finalHoldDuration", IntegerArgumentType.integer(0))
                                                                .then(argument("easing", EasingArgumentType.easing())
                                                                        .then(argument("yaw", FloatArgumentType.floatArg())
                                                                                .then(argument("pitch", FloatArgumentType.floatArg())
                                                                                        .then(argument("roll", FloatArgumentType.floatArg())
                                                                                                .executes(context -> CameraShakeServerCommand.addSequence(context, false))
                                                                                                .then(argument("posOffset", Vec3ArgumentType.vec3(false))
                                                                                                        .executes(context -> CameraShakeServerCommand.addSequence(context, true))
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(shakeSubCommand.build());
    }

    private static int createItem(CommandContext<ServerCommandSource> context, boolean hasMaxRange, boolean hasOriginPos, boolean hasTargetEntity, boolean hasMinFalloffRange) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw NO_USER.create();
        }
        Identifier sequenceIdentifier = IdentifierArgumentType.getIdentifier(context, "identifier");
        double maxRange = hasMaxRange ? DoubleArgumentType.getDouble(context, "maxRange") : -1;
        double minRange = hasMinFalloffRange ? DoubleArgumentType.getDouble(context, "falloffStartDistance") : 0;
        Vec3d pos = hasOriginPos ? Vec3ArgumentType.getVec3(context, "pos") : null;
        UUID entity = hasTargetEntity ? EntityArgumentType.getEntity(context, "entity").getUuid() : null;

        ItemStack stack = CameraDisplacementToolItem.createWithData(sequenceIdentifier, maxRange, minRange, pos, entity);
        player.getInventory().offerOrDrop(stack);
        context.getSource().sendFeedback(() -> Text.literal("Created new Camera Displacement Tool Item successfully"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int registerSequence(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");

        if (component.getEntryKeys().contains(identifier)) {
            throw DUPLICATE_ID.create();
        }

        component.modifyEntries(true, registry -> registry.put(identifier, new DisplacementSequence()));
        context.getSource().sendFeedback(() -> Text.literal("Created new Displacement Sequence: " + identifier.toString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeSequence(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");

        if (!component.getEntryKeys().contains(identifier)) {
            throw MISSING_ID.create();
        }

        component.modifyEntries(true, registry -> registry.remove(identifier));
        context.getSource().sendFeedback(() -> Text.literal("Removed Displacement Sequence: " + identifier.toString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeAllSequences(CommandContext<ServerCommandSource> context) {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);

        component.modifyEntries(true, HashMap::clear);

        context.getSource().sendFeedback(() -> Text.literal("Removed all Displacement Sequences"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int addSequence(CommandContext<ServerCommandSource> context, boolean includePositionDisplacement) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
        if (!component.getEntryKeys().contains(identifier)) {
            throw MISSING_ID.create();
        }

        int duration = IntegerArgumentType.getInteger(context, "duration");
        int finalHoldDuration = IntegerArgumentType.getInteger(context, "finalHoldDuration");
        Easing easing = EasingArgumentType.getEasing(context, "easing");
        Vector3f rotations = new Vector3f(
                FloatArgumentType.getFloat(context, "yaw"),
                FloatArgumentType.getFloat(context, "pitch"),
                FloatArgumentType.getFloat(context, "roll")
        );

        Displacement targetDisplacement = includePositionDisplacement ?
                new Displacement(Vec3ArgumentType.getVec3(context, "posOffset"), rotations) :
                new Displacement(rotations);

        component.modifyEntries(true, registry -> {
            DisplacementSequence sequence = registry.get(identifier);
            sequence.addEntry(targetDisplacement, duration, finalHoldDuration, easing);
            if (Easing.OSCILLATORS.contains(easing)) {
                sequence.addEntry(Displacement.DEFAULT, 1, 0, Easing.LINEAR);
            }
        });

        context.getSource().sendFeedback(() -> Text.literal("Added Displacement to \"%s\" Sequence".formatted(identifier.toString())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int startDisplacementSequence(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
        if (!component.getEntryKeys().contains(identifier)) {
            throw MISSING_ID.create();
        }

        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(identifier);
            ServerPlayNetworking.send(target, NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_START, buf);
        }

        context.getSource().sendFeedback(() -> Text.literal("Camera Displacement Sequence started for targets"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopDisplacementSequence(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
        DisplacementSequenceRegistryComponent component = DisplacementSequenceRegistryComponent.get(scoreboard);

        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
        if (!component.getEntryKeys().contains(identifier)) {
            throw MISSING_ID.create();
        }

        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(identifier);
            ServerPlayNetworking.send(target, NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_STOP, buf);
        }

        context.getSource().sendFeedback(() -> Text.literal("Camera Displacement Sequence stopped for targets"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopAllDisplacementSequences(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, NetworkIdentifiers.CAMERA_DISPLACEMENT_SEQUENCE_STOP_ALL, PacketByteBufs.empty());
        }

        context.getSource().sendFeedback(() -> Text.literal("All Camera Displacement Sequences stopped for targets"), true);
        return Command.SINGLE_SUCCESS;
    }
}
