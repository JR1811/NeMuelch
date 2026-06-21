package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.CrystalBlockItem;
import net.shirojr.nemuelch.item.custom.castAndMagicItem.MiasmaItem;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NeMuelchItemModificationCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType WRONG_ITEM_IN_MAINHAND =
            new SimpleCommandExceptionType(Text.literal("No data applied, use correct item in user's Mainhand ItemStack"));
    private static final SimpleCommandExceptionType INVALID_COLOR_FORMAT =
            new SimpleCommandExceptionType(Text.literal("Invalid color format. Use HEX rgb color values"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralCommandNode<ServerCommandSource> subCommand = literal("item")
                .then(literal("modify")
                        .then(literal("crystal")
                                .then(argument("innerColor", StringArgumentType.string())
                                        .then(argument("outerColor", StringArgumentType.string())
                                                .executes(NeMuelchItemModificationCommands::modifyCrystal)
                                        )
                                )
                        )
                        .then(literal("miasma")
                                .then(argument("innerColor", StringArgumentType.string())
                                        .then(argument("outerColor", StringArgumentType.string())
                                                .executes(NeMuelchItemModificationCommands::modifyMiasma)
                                        )
                                )
                        )
                )
                .build();
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand);
    }

    private static int modifyMiasma(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ItemStack stack = getMainHandStack(context);
        if (!(stack.getItem() instanceof MiasmaItem)) throw WRONG_ITEM_IN_MAINHAND.create();
        int innerColor = getColorAsInt(StringArgumentType.getString(context, "innerColor"));
        int outerColor = getColorAsInt(StringArgumentType.getString(context, "outerColor"));
        MiasmaItem.setColor(stack, MiasmaItem.Part.INNER, innerColor);
        MiasmaItem.setColor(stack, MiasmaItem.Part.OUTER, outerColor);
        setMainHandStack(context, stack);
        return Command.SINGLE_SUCCESS;
    }

    private static int modifyCrystal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ItemStack stack = getMainHandStack(context);
        if (!(stack.getItem() instanceof CrystalBlockItem)) throw WRONG_ITEM_IN_MAINHAND.create();
        int innerColor = getColorAsInt(StringArgumentType.getString(context, "innerColor"));
        int outerColor = getColorAsInt(StringArgumentType.getString(context, "outerColor"));
        CrystalBlockItem.setInnerColor(stack, innerColor);
        CrystalBlockItem.setOuterColor(stack, outerColor);
        setMainHandStack(context, stack);
        return Command.SINGLE_SUCCESS;
    }

    // region Util Methods
    private static int getColorAsInt(String hex) throws CommandSyntaxException {
        hex = hex.toLowerCase(Locale.ROOT).trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }

        if (!hex.matches("[0-9a-fA-F]{6}")) {
            throw INVALID_COLOR_FORMAT.create();
        }
        return ((int) Long.parseLong(hex, 16));
    }

    private static void setMainHandStack(CommandContext<ServerCommandSource> context, ItemStack stack) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw WRONG_ITEM_IN_MAINHAND.create();
        player.setStackInHand(Hand.MAIN_HAND, stack);
        context.getSource().sendFeedback(() -> Text.literal("Modified data on Mainhand ItemStack"), true);
    }

    private static ItemStack getMainHandStack(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw WRONG_ITEM_IN_MAINHAND.create();
        return player.getMainHandStack();
    }

    //endregion
}
