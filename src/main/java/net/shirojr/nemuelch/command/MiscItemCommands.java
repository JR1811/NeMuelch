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
import net.minecraft.command.argument.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@SuppressWarnings("SameParameterValue")
public class MiscItemCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType MISSING_PLAYER_EXECUTION =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by Player"));
    private static final SimpleCommandExceptionType NO_VALID_TARGET_FOUND =
            new SimpleCommandExceptionType(Text.literal("No valid Target found"));
    private static final SimpleCommandExceptionType ENTRY_DUPLICATE =
            new SimpleCommandExceptionType(Text.literal("Entry already exists"));
    private static final SimpleCommandExceptionType INVALID_ENTRY =
            new SimpleCommandExceptionType(Text.literal("Entry is invalid"));
    private static final SimpleCommandExceptionType MISSING_ENTRY =
            new SimpleCommandExceptionType(Text.literal("Entry is missing"));
    private static final SimpleCommandExceptionType MISSING_DATA =
            new SimpleCommandExceptionType(Text.literal("Data is missing"));
    private static final SimpleCommandExceptionType MAIN_STACK_EMPTY =
            new SimpleCommandExceptionType(Text.literal("Mainhand ItemStack is empty"));
    private static final SimpleCommandExceptionType NO_DATA =
            new SimpleCommandExceptionType(Text.literal("No data"));

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

        itemRoot.addChild(literal("hiddenEnchantments")
                .then(literal("add")
                        .then(argument("enchantment", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENCHANTMENT))
                                .executes(MiscItemCommands::addHiddenEnchantment)
                        )
                )
                .then(literal("print")
                        .executes(MiscItemCommands::printHiddenEnchantments)
                )
                .then(literal("remove")
                        .executes(context -> MiscItemCommands.removeHiddenEnchantments(context, null))
                        .then(argument("enchantment", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENCHANTMENT))
                                .executes(context ->
                                        MiscItemCommands.removeHiddenEnchantments(context, RegistryEntryArgumentType.getEnchantment(context, "enchantment"))
                                )
                        )
                )
                .build()
        );
    }

    @SuppressWarnings("SameReturnValue")
    private static int removeHiddenEnchantments(CommandContext<ServerCommandSource> context, @Nullable RegistryEntry.Reference<Enchantment> enchantment) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw MISSING_PLAYER_EXECUTION.create();
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        NbtCompound nbt = stack.getNbt();
        if (enchantment == null) {
            if (nbt != null) {
                nbt.remove(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS);
                finalizeCommand(context);
                return Command.SINGLE_SUCCESS;
            } else {
                throw MISSING_DATA.create();
            }
        }
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS)) {
            throw MISSING_DATA.create();
        }
        NbtList oldNbtList = nbt.getList(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS, NbtElement.STRING_TYPE);
        NbtList newNbtList = new NbtList();
        for (int i = 0; i < oldNbtList.size(); i++) {
            Identifier hiddenEnchantmentId = Identifier.tryParse(oldNbtList.getString(i));
            if (hiddenEnchantmentId == null || enchantment.matchesId(hiddenEnchantmentId)) continue;
            newNbtList.add(NbtString.of(hiddenEnchantmentId.toString()));
        }
        if (newNbtList.isEmpty()) {
            nbt.remove(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS);
        } else if (oldNbtList.equals(newNbtList)) {
            throw MISSING_ENTRY.create();
        }
        else {
            nbt.put(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS, newNbtList);
        }
        finalizeCommand(context);
        return Command.SINGLE_SUCCESS;
    }

    private static int printHiddenEnchantments(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw MISSING_PLAYER_EXECUTION.create();
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS)) throw NO_DATA.create();
        NbtList nbtList = nbt.getList(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS, NbtElement.STRING_TYPE);
        context.getSource().sendFeedback(() -> Text.literal("Hidden Enchantments on Mainhand ItemStack:"), true);
        for (int i = 0; i < nbtList.size(); i++) {
            Identifier enchantmentId = Identifier.tryParse(nbtList.getString(i));
            if (enchantmentId == null) continue;
            Enchantment enchantment = Registries.ENCHANTMENT.getOrEmpty(enchantmentId).orElseThrow(INVALID_ENTRY::create);
            MutableText line = Text.literal(" - ").append(enchantment.getName(1)).append(Text.literal(" (%s)".formatted(enchantmentId.toString())));
            context.getSource().sendFeedback(() -> line.styled(style -> style.withColor(Formatting.GRAY)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addHiddenEnchantment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        RegistryEntry<Enchantment> enchantment = RegistryEntryArgumentType.getEnchantment(context, "enchantment");
        Optional<RegistryKey<Enchantment>> newEnchantmentKey = enchantment.getKey();
        if (newEnchantmentKey.isEmpty()) {
            throw INVALID_ENTRY.create();
        }
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw MISSING_PLAYER_EXECUTION.create();
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList updatedHiddenEnchantsNbtList = new NbtList();

        if (nbt.contains(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS)) {
            NbtList oldHiddenEnchantmentsNbtList = nbt.getList(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS, NbtElement.STRING_TYPE);
            for (int i = 0; i < oldHiddenEnchantmentsNbtList.size(); i++) {
                Identifier hiddenEnchantmentId = Identifier.tryParse(oldHiddenEnchantmentsNbtList.getString(i));
                if (hiddenEnchantmentId == null) continue;
                if (enchantment.matchesId(hiddenEnchantmentId)) {
                    throw ENTRY_DUPLICATE.create();
                }
                updatedHiddenEnchantsNbtList.add(NbtString.of(hiddenEnchantmentId.toString()));
            }
        }
        updatedHiddenEnchantsNbtList.add(NbtString.of(newEnchantmentKey.get().getValue().toString()));
        nbt.put(NeMuelchNbtKeys.HIDDEN_ENCHANTMENTS, updatedHiddenEnchantsNbtList);
        finalizeCommand(context);
        return Command.SINGLE_SUCCESS;
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
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putBoolean("Unbreakable", unbreakable);
        return finalizeCommand(context);
    }

    private static int glint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        boolean glint = BoolArgumentType.getBool(context, "glint");
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        NbtCompound nbt = stack.getOrCreateNbt();
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
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        int damage = Math.min(IntegerArgumentType.getInteger(context, "durability"), stack.getMaxDamage());
        stack.setDamage(stack.getMaxDamage() - damage);
        return finalizeCommand(context);
    }

    private static int reDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        int damage = Math.min(IntegerArgumentType.getInteger(context, "damage"), stack.getMaxDamage());
        stack.setDamage(damage);
        return finalizeCommand(context);
    }


    private static int reAuthor(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        String author = StringArgumentType.getString(context, "author");
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        stack.setSubNbt("author", NbtString.of(author));
        return finalizeCommand(context);
    }

    private static int reLore(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw MISSING_PLAYER_EXECUTION.create();
        }
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();

        Text lore = TextArgumentType.getTextArgument(context, "lore");

        NbtCompound nbt = stack.getOrCreateNbt();
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
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) throw MAIN_STACK_EMPTY.create();
        Text name = TextArgumentType.getTextArgument(context, "name");
        stack.setCustomName(name);
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
        context.getSource().sendFeedback(() -> Text.literal("Successfully changed item data of Main Hand ItemStack"), true);
        return Command.SINGLE_SUCCESS;
    }
}
