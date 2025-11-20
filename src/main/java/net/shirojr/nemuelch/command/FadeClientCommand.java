package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaders;
import net.shirojr.nemuelch.compat.satin.shaders.FadeShaderManager;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class FadeClientCommand {
    private static final SimpleCommandExceptionType TARGET_NOT_APPLICABLE =
            new SimpleCommandExceptionType(Text.literal("Not applicable"));
    private static final SimpleCommandExceptionType SATIN_API_NOT_PRESENT =
            new SimpleCommandExceptionType(Text.literal("Satin API not installed"));

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("fade")
                .then(literal("toBlack")
                        .then(argument("durationFrames", IntegerArgumentType.integer(0))
                                .executes(FadeClientCommand::toBlack)
                        )
                )
                .then(literal("fromBlack")
                        .then(argument("durationFrames", IntegerArgumentType.integer(0))
                                .executes(FadeClientCommand::fromBlack)
                        )
                )
                .then(literal("set")
                        .then(argument("amount", FloatArgumentType.floatArg(0))
                                .executes(FadeClientCommand::setAmount)
                        )
                )
        );
    }

    private static int setAmount(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) throw TARGET_NOT_APPLICABLE.create();
        if (!NeMuelch.isSatinPresent()) throw SATIN_API_NOT_PRESENT.create();

        NeMuelchShaders.FADE.setStaticFadeAmount(FloatArgumentType.getFloat(fabricClientCommandSourceCommandContext, "amount"));
        fabricClientCommandSourceCommandContext.getSource().sendFeedback(Text.literal("Current Shader Fade amount: " + FadeShaderManager.getCurrentFade()));
        return Command.SINGLE_SUCCESS;
    }

    private static int toBlack(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) throw TARGET_NOT_APPLICABLE.create();
        if (!NeMuelch.isSatinPresent()) throw SATIN_API_NOT_PRESENT.create();

        int durationFrames = IntegerArgumentType.getInteger(context, "durationFrames");
        NeMuelchShaders.FADE.fadeToBlack(durationFrames);

        return Command.SINGLE_SUCCESS;
    }

    private static int fromBlack(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) throw TARGET_NOT_APPLICABLE.create();
        if (!NeMuelch.isSatinPresent()) throw SATIN_API_NOT_PRESENT.create();

        int durationFrames = IntegerArgumentType.getInteger(context, "durationFrames");
        NeMuelchShaders.FADE.fadeFromBlack(durationFrames);

        return Command.SINGLE_SUCCESS;
    }
}
