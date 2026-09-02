package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.component.ActCommandComponent;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NemuelchGameRules;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ActCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType SOURCE_NO_PLAYER =
            new SimpleCommandExceptionType(Text.literal("Command not executed by player and misses source position"));
    public static final DynamicCommandExceptionType TOO_MUCH_CONTENT = new DynamicCommandExceptionType(maxCount ->
            Text.literal("Too many characters (Max: %s)".formatted(maxCount))
    );
    private static final SuggestionProvider<ServerCommandSource> ACT_TARGET_EXAMPLES =
            (context, builder) -> {
                builder.suggest("@a");
                builder.suggest("@e[type=player,distance=..6]");
                builder.suggest("@e[type=player,name=!\"JohnSmith1234\"]");
                return builder.buildFuture();
            };

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("act")
                .then(argument("content", StringArgumentType.string())
                        .executes(context -> ActCommand.actDefault(context, false))
                        .then(argument("targets", EntityArgumentType.players())
                                .suggests(ACT_TARGET_EXAMPLES)
                                .executes(context -> ActCommand.actWithTargets(context, false, null))
                        )
                )
                .then(literal("incognito").requires(source -> source.hasPermissionLevel(2))
                        .then(argument("content", StringArgumentType.string())
                                .executes(context -> ActCommand.actDefault(context, true))
                                .then(argument("targets", EntityArgumentType.players())
                                        .suggests(ACT_TARGET_EXAMPLES)
                                        .executes(context -> ActCommand.actWithTargets(context, true, null))
                                        .then(argument("sourcePos", Vec3ArgumentType.vec3())
                                                .executes(context ->
                                                        ActCommand.actWithTargets(context, true, Vec3ArgumentType.getVec3(context, "sourcePos"))
                                                )
                                        )
                                )
                        )
                )
        );
        dispatcher.register(literal("act-debug").requires(source -> source.hasPermissionLevel(2))
                .then(literal("stalk")
                        .then(argument("stalk", BoolArgumentType.bool())
                                .executes(ActCommand::runStalkToggle)
                        )
                )
        );
    }

    private static int actDefault(CommandContext<ServerCommandSource> context, boolean incognito) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerWorld world = context.getSource().getWorld();
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null && !incognito) {
            throw SOURCE_NO_PLAYER.create();
        }

        double maxDistance = server.getGameRules().get(NemuelchGameRules.ACT_MAX_DISTANCE).get();
        Collection<ServerPlayerEntity> around = PlayerLookup.around(world, context.getSource().getPosition(), maxDistance);

        sendText(context, incognito, around, StringArgumentType.getString(context, "content"));
        return Command.SINGLE_SUCCESS;
    }

    private static int actWithTargets(CommandContext<ServerCommandSource> context, boolean incognito, @Nullable Vec3d sourcePos) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null && !incognito) {
            throw SOURCE_NO_PLAYER.create();
        }
        if (sourcePos == null) {
            sourcePos = context.getSource().getPosition();
        }

        HashSet<ServerPlayerEntity> targetsInMaxRange = new HashSet<>();
        double maxDistance = server.getGameRules().get(NemuelchGameRules.ACT_MAX_DISTANCE).get();

        for (ServerPlayerEntity target : EntityArgumentType.getPlayers(context, "targets")) {
            if (sourcePos.squaredDistanceTo(target.getPos()) > maxDistance * maxDistance) continue;
            targetsInMaxRange.add(target);
        }


        sendText(context, incognito, targetsInMaxRange, StringArgumentType.getString(context, "content"));
        return Command.SINGLE_SUCCESS;
    }

    private static int runStalkToggle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw SOURCE_NO_PLAYER.create();
        }
        boolean stalk = BoolArgumentType.getBool(context, "stalk");

        ActCommandComponent actCommandComponent = ActCommandComponent.get(player);
        actCommandComponent.setStalkMode(stalk);

        context.getSource().sendFeedback(() -> Text.literal("Stalk all /act commands: " + actCommandComponent.enabledStalkMode()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void sendText(CommandContext<ServerCommandSource> context, boolean incognito,
                                 Collection<ServerPlayerEntity> targets, String content) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayerEntity source = context.getSource().getPlayer();
        int maxLength = server.getGameRules().getInt(NemuelchGameRules.ACT_MAX_LENGTH);
        if (content.length() > maxLength) {
            throw TOO_MUCH_CONTENT.create(maxLength);
        }
        HashSet<ServerPlayerEntity> receivers = new HashSet<>(targets);
        if (source != null) receivers.add(source);
        for (ServerPlayerEntity entry : PlayerLookup.all(server)) {
            if (!entry.hasPermissionLevel(2)) continue;
            ActCommandComponent actCommandComponent = ActCommandComponent.get(entry);
            if (!actCommandComponent.enabledStalkMode()) continue;
            receivers.add(entry);
        }

        for (ServerPlayerEntity target : receivers) {
            String output = "";
            if (source != null && !incognito) {
                output += "§6[%s]§r ".formatted(source.getName().getString());
            }
            output += content;
            MutableText text = Text.literal(output).formatted(Formatting.ITALIC);
            if (NeMuelchConfigInit.CONFIG.printActCommandInChat) {
                target.sendMessage(text, false);
            }
            if (NeMuelchConfigInit.CONFIG.printActCommandInActionBar) {
                target.sendMessage(text, true);
            }
        }
    }
}
