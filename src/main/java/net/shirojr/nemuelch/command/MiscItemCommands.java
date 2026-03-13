package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameParameterValue")
public class MiscItemCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType MISSING_PLAYER_EXECUTION =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by Player"));
    private static final SimpleCommandExceptionType NO_VALID_TARGET_FOUND =
            new SimpleCommandExceptionType(Text.literal("No valid Target found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        CommandNode<ServerCommandSource> itemRoot = commandDispatcher.getRoot().getChild("item");
        if (itemRoot == null) return;
        itemRoot.addChild(buildTextCommand("rename", "name", MiscItemCommands::reName));
        itemRoot.addChild(buildTextCommand("relore", "lore", MiscItemCommands::reLore));
        itemRoot.addChild(buildStringCommand("reauthor", "author", MiscItemCommands::reAuthor));
        itemRoot.addChild(buildIntCommand("redamage", "damage", MiscItemCommands::reDamage));
        itemRoot.addChild(buildIntCommand("redurability", "durability", MiscItemCommands::reDurability));
        itemRoot.addChild(buildBooleanCommand("glint", "glint", MiscItemCommands::glint));
        itemRoot.addChild(buildBooleanCommand("unbreakable", "unbreakable", MiscItemCommands::unbreakable));
        itemRoot.addChild(buildEntityCommand("dropall", "target", MiscItemCommands::dropAll));

        itemRoot.addChild(literal("killaura")
                .then(argument("hosts", EntityArgumentType.entities())
                        .then(argument("radius", FloatArgumentType.floatArg(0.0001f))
                                .then(argument("duration", IntegerArgumentType.integer(0))
                                        .executes(context ->
                                                MiscItemCommands.setKillAura(
                                                        context,
                                                        EntityArgumentType.getEntities(context, "hosts"),
                                                        FloatArgumentType.getFloat(context, "radius"),
                                                        IntegerArgumentType.getInteger(context, "duration"),
                                                        null
                                                )
                                        )
                                        .then(argument("filter", ItemPredicateArgumentType.itemPredicate(commandRegistryAccess))
                                                .executes(context ->
                                                        MiscItemCommands.setKillAura(
                                                                context,
                                                                EntityArgumentType.getEntities(context, "hosts"),
                                                                FloatArgumentType.getFloat(context, "radius"),
                                                                IntegerArgumentType.getInteger(context, "duration"),
                                                                ItemPredicateArgumentType.getItemStackPredicate(context, "filter")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .build()
        );

        itemRoot.addChild(literal("cooldown").then(argument("targets", EntityArgumentType.entities())
                .then(argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                        .then(argument("ticks", IntegerArgumentType.integer(0))
                                .executes(MiscItemCommands::reCooldown)
                        )
                )
        ).build());
    }

    private static int setKillAura(CommandContext<ServerCommandSource> context, Collection<? extends Entity> hosts,
                                   float radius, int duration, @Nullable Predicate<ItemStack> itemFilter) throws CommandSyntaxException {
        boolean addedKillAura = false;
        for (Entity host : hosts) {
            if (!(host instanceof LivingEntity entity)) continue;
            addedKillAura = true;
            MiscEntityComponent component = MiscEntityComponent.get(entity);
            component.setItemEntityKillAuraDuration(duration);
            component.setItemEntityKillAuraRadius(radius);
            component.setItemEntityKillAuraFilter(itemFilter);
            context.getSource().sendFeedback(() ->
                    Text.literal("%s kills ItemEntities in radius %s for %s ticks".formatted(
                            host.getName().getString(), radius, duration
                    )), true);
        }
        if (!addedKillAura) {
            throw NO_VALID_TARGET_FOUND.create();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int unbreakable(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        boolean unbreakable = BoolArgumentType.getBool(context, "unbreakable");
        ItemStack mainHandStack = player.getMainHandStack();
        NbtCompound nbt = mainHandStack.getOrCreateNbt();
        nbt.putBoolean("Unbreakable", unbreakable);
        return finalizeCommand(context);
    }

    private static int glint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        boolean glint = BoolArgumentType.getBool(context, "glint");
        ItemStack mainHandStack = player.getMainHandStack();

        NbtCompound nbt = mainHandStack.getOrCreateNbt();
        NbtCompound displayNbt = nbt.contains(ItemStack.DISPLAY_KEY) ? nbt.getCompound(ItemStack.DISPLAY_KEY) : new NbtCompound();
        displayNbt.putBoolean("glint", glint);
        nbt.put(ItemStack.DISPLAY_KEY, displayNbt);

        return finalizeCommand(context);
    }

    private static int dropAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        StringBuilder sb = new StringBuilder();
        for (Entity entity : EntityArgumentType.getEntities(context, "target")) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(entity.getName().getString());
            if (entity instanceof PlayerEntity player) {
                player.getInventory().dropAll();
            } else if (entity instanceof LivingEntity livingEntity) {
                for (ItemStack stack : livingEntity.getItemsEquipped()) {
                    if (stack == null || stack.isEmpty()) continue;
                    livingEntity.dropStack(stack.copy());
                    stack.setCount(0);
                }
            }
        }
        context.getSource().sendFeedback(() -> Text.literal("Dropped all items for " + sb), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int reDurability(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        ItemStack mainHandStack = player.getMainHandStack();
        int damage = Math.min(IntegerArgumentType.getInteger(context, "durability"), mainHandStack.getMaxDamage());
        mainHandStack.setDamage(mainHandStack.getMaxDamage() - damage);
        return finalizeCommand(context);
    }

    private static int reDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        ItemStack mainHandStack = player.getMainHandStack();
        int damage = Math.min(IntegerArgumentType.getInteger(context, "damage"), mainHandStack.getMaxDamage());
        mainHandStack.setDamage(damage);
        return finalizeCommand(context);
    }


    private static int reAuthor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        String author = StringArgumentType.getString(context, "author");
        ItemStack mainHandStack = player.getMainHandStack();
        mainHandStack.setSubNbt("author", NbtString.of(author));

        return finalizeCommand(context);
    }

    private static int reLore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        Text lore = TextArgumentType.getTextArgument(context, "lore");

        ItemStack mainHandStack = player.getMainHandStack();

        NbtCompound nbt = mainHandStack.getOrCreateNbt();
        NbtCompound displayNbt = nbt.contains(ItemStack.DISPLAY_KEY) ? nbt.getCompound(ItemStack.DISPLAY_KEY) : new NbtCompound();
        NbtList loreListNbt = new NbtList();

        String jsonLore = Text.Serializer.toJson(lore);
        loreListNbt.add(NbtString.of(jsonLore));
        displayNbt.put(ItemStack.LORE_KEY, loreListNbt);

        nbt.put(ItemStack.DISPLAY_KEY, displayNbt);

        return finalizeCommand(context);
    }

    private static int reName(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        Text name = TextArgumentType.getTextArgument(context, "name");
        ItemStack mainHandStack = player.getMainHandStack();
        mainHandStack.setCustomName(name);
        return finalizeCommand(context);
    }

    private static int reCooldown(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        StringBuilder sb = new StringBuilder();
        int validEntries = 0;
        for (Entity entity : EntityArgumentType.getEntities(context, "targets")) {
            if (!(entity instanceof PlayerEntity player)) continue;
            player.getItemCooldownManager().set(item, ticks);
            if (validEntries > 0) {
                sb.append(", ");
            }
            validEntries++;
            sb.append(player.getName().getString());
        }
        if (validEntries > 0) {
            context.getSource().sendFeedback(() -> Text.literal("Applied %s ticks cooldown to %s for ".formatted(ticks, item.getName().getString()) + sb), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("No cooldown was applied"), true);
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }


    private static LiteralCommandNode<ServerCommandSource> buildTextCommand(String commandName, String argumentName,
                                                                            Command<ServerCommandSource> executes) {
        return literal(commandName).requires(source -> source.hasPermissionLevel(2))
                .then(argument(argumentName, TextArgumentType.text()).executes(executes)).build();
    }

    private static LiteralCommandNode<ServerCommandSource> buildStringCommand(String commandName, String argumentName,
                                                                              Command<ServerCommandSource> executes) {
        return literal(commandName).requires(source -> source.hasPermissionLevel(2))
                .then(argument(argumentName, StringArgumentType.string()).executes(executes)).build();
    }

    private static LiteralCommandNode<ServerCommandSource> buildIntCommand(String commandName, String argumentName,
                                                                           Command<ServerCommandSource> executes) {
        return literal(commandName).requires(source -> source.hasPermissionLevel(2))
                .then(argument(argumentName, IntegerArgumentType.integer()).executes(executes)).build();
    }

    private static CommandNode<ServerCommandSource> buildEntityCommand(String commandName, String argumentName,
                                                                       Command<ServerCommandSource> executes) {
        return literal(commandName).requires(source -> source.hasPermissionLevel(2))
                .then(argument(argumentName, EntityArgumentType.entities()).executes(executes)).build();
    }

    private static CommandNode<ServerCommandSource> buildBooleanCommand(String commandName, String argumentName,
                                                                        Command<ServerCommandSource> executes) {
        return literal(commandName).requires(source -> source.hasPermissionLevel(2))
                .then(argument(argumentName, BoolArgumentType.bool()).executes(executes)).build();
    }

    private static int finalizeCommand(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Successfully applied item data to Main Hand ItemStack"), true);
        return Command.SINGLE_SUCCESS;
    }
}
