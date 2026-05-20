package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.BlockFinderComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlockFinderCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_PLAYERS =
            new SimpleCommandExceptionType(Text.literal("No players were found"));
    private static final SimpleCommandExceptionType MISSING_PLAYER =
            new SimpleCommandExceptionType(Text.literal("Block Finder can only be used by players"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("blockFinder")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("toggle")
                        .then(argument("active", BoolArgumentType.bool())
                                .executes(context -> BlockFinderCommands.toggle(context, null))
                                .then(argument("targets", EntityArgumentType.players())
                                        .executes(context -> BlockFinderCommands.toggle(context, EntityArgumentType.getPlayers(context, "targets")))
                                )
                        )
                )
                .then(literal("radius")
                        .then(argument("value", IntegerArgumentType.integer(0, BlockFinderComponent.MAX_RANGE))
                                .executes(BlockFinderCommands::setRadius)
                        )
                )
                .then(literal("print")
                        .executes(BlockFinderCommands::print)
                )
                .then(literal("criteria")
                        .then(literal("hasInventory")
                                .executes(context ->
                                        BlockFinderCommands.criteria(context, BlockFinderComponent.STORAGE_SEARCH_CRITERIA)
                                )
                        )
                        .then(literal("hasNonEmptyInventory")
                                .executes(context ->
                                        BlockFinderCommands.criteria(context, BlockFinderComponent.NON_EMPTY_STORAGE_SEARCH_CRITERIA)
                                )
                        )
                        .then(literal("hasItemInInventory")
                                .then(argument("predicate", ItemPredicateArgumentType.itemPredicate(registryAccess))
                                        .executes(context -> {
                                                    Predicate<ItemStack> predicate = ItemPredicateArgumentType.getItemStackPredicate(context, "predicate");
                                                    return BlockFinderCommands.criteria(context, BlockFinderComponent.ITEM_SEARCH_CRITERIA.apply(predicate));
                                                }
                                        )
                                )
                        )
                        .then(literal("custom")
                                .then(argument("predicate", BlockPredicateArgumentType.blockPredicate(registryAccess))
                                        .executes(context ->
                                                BlockFinderCommands.criteria(context, BlockPredicateArgumentType.getBlockPredicate(context, "predicate"))
                                        )
                                )
                        )
                        .then(literal("clear")
                                .executes(context ->
                                        BlockFinderCommands.criteria(context, BlockFinderComponent.EMPTY_SEARCH_CRITERIA)
                                )
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int print(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<ServerPlayerEntity> activeUsers = new ArrayList<>();
        for (ServerPlayerEntity player : PlayerLookup.all(context.getSource().getServer())) {
            BlockFinderComponent component = BlockFinderComponent.get(player);
            if (component.isActive()) activeUsers.add(player);
        }
        if (activeUsers.isEmpty()) {
            throw NO_PLAYERS.create();
        }
        context.getSource().sendFeedback(() -> Text.literal("Block Finder is active for:"), true);
        MutableText names = Text.literal("");
        for (int i = 0; i < activeUsers.size(); i++) {
            ServerPlayerEntity user = activeUsers.get(i);
            names.append(Text.literal(user.getName().getString()));
            if (i < activeUsers.size() - 1) {
                names.append(", ");
            }
        }
        context.getSource().sendFeedback(() -> names, true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setRadius(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER.create();
        }
        int radius = IntegerArgumentType.getInteger(context, "value");

        BlockFinderComponent component = BlockFinderComponent.get(player);
        component.setRadius(radius);

        context.getSource().sendFeedback(() -> Text.translatable("Set Block Finder radius for %s to %s".formatted(
                player.getName().getString(), radius
        )), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int criteria(CommandContext<ServerCommandSource> context, Predicate<CachedBlockPosition> predicate) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER.create();
        }
        if (predicate == null) {
            predicate = BlockFinderComponent.STORAGE_SEARCH_CRITERIA;
        }
        BlockFinderComponent component = BlockFinderComponent.get(player);
        component.setSearchCriteria(predicate);
        context.getSource().sendFeedback(() -> Text.literal("Set Block Finder Criteria for " + player.getName().getString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int toggle(CommandContext<ServerCommandSource> context, @Nullable Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        if (targets == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player == null) {
                throw MISSING_PLAYER.create();
            }
            targets = List.of(player);
        } else if (targets.isEmpty()) {
            throw MISSING_PLAYER.create();
        }
        boolean active = BoolArgumentType.getBool(context, "active");
        for (ServerPlayerEntity target : targets) {
            BlockFinderComponent component = BlockFinderComponent.get(target);
            component.setActive(active);
            context.getSource().sendFeedback(() -> Text.literal("Activated Block Finder for %s: %s".formatted(
                    target.getName().getString(), active
            )), true);
        }
        return Command.SINGLE_SUCCESS;
    }
}
