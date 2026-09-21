package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.DescriptionEntityComponent;
import net.shirojr.nemuelch.compat.cca.util.DescriptionData;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DescribeCommands implements CommandRegistrationCallback {
    public static final DynamicCommandExceptionType TOO_MUCH_CONTENT = new DynamicCommandExceptionType(maxCount ->
            Text.literal("Too many characters (Max: %s)".formatted(maxCount))
    );
    public static final DynamicCommandExceptionType ON_CLIPBOARD_PACKET_COOLDOWN = new DynamicCommandExceptionType(cooldown ->
            Text.literal("Your requested Clipboard was rate limited! (%s cooldown seconds left)".formatted((int) cooldown / 20))
    );
    public static final SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(Text.literal("Feature is disabled"));
    public static final SimpleCommandExceptionType NO_SENDER = new SimpleCommandExceptionType(Text.literal("No sender to attach to"));
    public static final SimpleCommandExceptionType NO_USER = new SimpleCommandExceptionType(Text.literal("No user specified"));


    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("describe")
                .then(literal("create")
                        .then(argument("content", StringArgumentType.string())
                                .executes(context -> DescribeCommands.describeDefault(context, -1, null, null))
                                .then(argument("tickDuration", IntegerArgumentType.integer(-1))
                                        .suggests((context, builder) -> {
                                            builder.suggest(-1);
                                            builder.suggest(20);
                                            builder.suggest(6000);
                                            return builder.buildFuture();
                                        })
                                        .then(argument("targets", EntityArgumentType.players())
                                                .executes(context ->
                                                        DescribeCommands.describeDefault(context, IntegerArgumentType.getInteger(context, "tickDuration"), EntityArgumentType.getPlayers(context, "targets"), null)
                                                )
                                                .then(argument("sources", EntityArgumentType.players()).requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                                                        .executes(context ->
                                                                DescribeCommands.describeDefault(context, IntegerArgumentType.getInteger(context, "tickDuration"), EntityArgumentType.getPlayers(context, "targets"), EntityArgumentType.getPlayers(context, "sources"))
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("createFromClipBoard")
                        .executes(context -> DescribeCommands.describeFromClipBoard(context, -1, null, null))
                        .then(argument("tickDuration", IntegerArgumentType.integer(0))
                                .suggests((context, builder) -> {
                                    builder.suggest(-1);
                                    builder.suggest(400);
                                    builder.suggest(6000);
                                    return builder.buildFuture();
                                })
                                .executes(context ->
                                        DescribeCommands.describeFromClipBoard(context, IntegerArgumentType.getInteger(context, "tickDuration"), null, null)
                                )
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context ->
                                                DescribeCommands.describeFromClipBoard(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "tickDuration"),
                                                        EntityArgumentType.getPlayers(context, "targets"),
                                                        null
                                                )
                                        )
                                        .then(argument("sourceClient", EntityArgumentType.player()).requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                                                .executes(context ->
                                                        DescribeCommands.describeFromClipBoard(
                                                                context,
                                                                IntegerArgumentType.getInteger(context, "tickDuration"),
                                                                EntityArgumentType.getPlayers(context, "targets"),
                                                                EntityArgumentType.getPlayer(context, "sourceClient")
                                                        )
                                                )

                                        )
                                )
                        )
                )
                .then(literal("clear")
                        .executes(context -> DescribeCommands.describeClear(context, null))
                        .then(argument("targets", EntityArgumentType.players()).requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                                .executes(context ->
                                        DescribeCommands.describeClear(context, EntityArgumentType.getPlayers(context, "targets"))
                                )
                        )
                )
                .then(literal("disable")
                        .then(argument("disable", BoolArgumentType.bool())
                                .executes(context ->
                                        DescribeCommands.describeDisable(context, null)
                                )
                                .then(argument("targets", EntityArgumentType.players()).requires(NeMuelchCommandUtil.HIGHER_PERMISSION_LEVEL)
                                        .executes(context ->
                                                DescribeCommands.describeDisable(context, EntityArgumentType.getPlayers(context, "targets"))
                                        )
                                )
                        )
                );

        dispatcher.register(command);
        dispatcher.register(literal("desc").redirect(command.build()));
    }

    private static int describeClear(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        if (targets == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) throw NO_USER.create();
            targets = List.of(player);
        }
        if (targets.isEmpty()) {
            throw NO_USER.create();
        }
        for (ServerPlayerEntity target : targets) {
            DescriptionEntityComponent component = DescriptionEntityComponent.get(target);
            component.setData(null, true);
            context.getSource().sendFeedback(() -> Text.literal("Cleared description of " + target.getName().getString()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int describeFromClipBoard(CommandContext<ServerCommandSource> context, int duration, @Nullable Collection<ServerPlayerEntity> targets, @Nullable ServerPlayerEntity source) throws CommandSyntaxException {
        ServerPlayerEntity sourcePlayer = source != null ? source : context.getSource().getPlayer();
        if (sourcePlayer == null) {
            throw NO_USER.create();
        }
        DescriptionEntityComponent component = DescriptionEntityComponent.get(sourcePlayer);
        if (component.isOnClipboardPacketCooldown()) {
            throw ON_CLIPBOARD_PACKET_COOLDOWN.create(component.getClipboardPacketCooldown());
        }

        List<UUID> targetUuids = targets != null ? new ArrayList<>() : null;
        if (targetUuids != null) {
            targets.forEach(player -> targetUuids.add(player.getUuid()));
        }

        MinecraftServer server = context.getSource().getServer();
        int maxLength = server.getGameRules().getInt(NemuelchGameRules.DESCRIBE_MAX_LENGTH);
        component.requestClipboardFromClient(duration, maxLength, targetUuids);
        return Command.SINGLE_SUCCESS;
    }

    private static int describeDefault(CommandContext<ServerCommandSource> context, int duration, @Nullable Collection<ServerPlayerEntity> targets, @Nullable Collection<ServerPlayerEntity> sources)
            throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        if (!server.getGameRules().getBoolean(NemuelchGameRules.DESCRIBE_ENABLED)) {
            throw DISABLED.create();
        }
        if (sources == null) {
            ServerPlayerEntity sourcePlayer = context.getSource().getPlayer();
            if (sourcePlayer == null) throw NO_SENDER.create();
            sources = List.of(sourcePlayer);
        }
        String content = StringArgumentType.getString(context, "content");
        int contentMaxLength = server.getGameRules().getInt(NemuelchGameRules.DESCRIBE_MAX_LENGTH);
        if (content.length() > contentMaxLength) {
            throw TOO_MUCH_CONTENT.create(contentMaxLength);
        }
        double maxDistance = server.getGameRules().get(NemuelchGameRules.DESCRIBE_MAX_DISTANCE).get();
        double maxDeviationAngle = server.getGameRules().get(NemuelchGameRules.DESCRIBE_MAX_DEVIATION_ANGLE).get();
        List<UUID> allowedViewers = targets == null ? null : new ArrayList<>();
        if (allowedViewers != null) {
            targets.forEach(player -> allowedViewers.add(player.getUuid()));
        }
        for (ServerPlayerEntity source : sources) {
            DescriptionEntityComponent component = DescriptionEntityComponent.get(source);
            component.setData(new DescriptionData(Text.of(content), allowedViewers, duration, maxDistance, (float) maxDeviationAngle), true);
            context.getSource().sendFeedback(() -> Text.literal("Set description content for " + source.getName().getString()), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int describeDisable(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> specifiedPlayers) throws CommandSyntaxException {
        ServerPlayerEntity user = context.getSource().getPlayer();
        if (user == null && specifiedPlayers == null) {
            throw NO_USER.create();
        }
        List<ServerPlayerEntity> players = new ArrayList<>();
        if (specifiedPlayers != null) players.addAll(specifiedPlayers);
        else players.add(user);
        boolean disable = BoolArgumentType.getBool(context, "disable");

        for (ServerPlayerEntity player : players) {
            DescriptionEntityComponent component = DescriptionEntityComponent.get(player);
            component.setHideAllDescriptions(disable, true);
            context.getSource().sendFeedback(() -> Text.literal("%s stops seeing any Entity Descriptions: %s".formatted(player.getName().getString(), disable)), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
