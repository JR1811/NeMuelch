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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.AcidEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AcidCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_TARGETS =
            new SimpleCommandExceptionType(Text.literal("No targets found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> acidSubCommand = literal("acid")
                .then(literal("entity")
                        .then(literal("info")
                                .executes(context -> AcidCommands.info(context, null))
                                .then(argument("targets", EntityArgumentType.entities())
                                        .executes(context ->
                                                AcidCommands.info(context, EntityArgumentType.getEntities(context, "targets"))
                                        )
                                )
                        )
                        .then(literal("clear")
                                .executes(context -> AcidCommands.clearAcid(context, null))
                                .then(argument("targets", EntityArgumentType.entities())
                                        .executes(context ->
                                                AcidCommands.clearAcid(context, EntityArgumentType.getEntities(context, "targets"))
                                        )
                                )
                        )
                        .then(literal("setTick")
                                .then(argument("atmosphericTick", IntegerArgumentType.integer(0))
                                        .executes(context ->
                                                AcidCommands.setAtmosphericAcidTick(context, IntegerArgumentType.getInteger(context, "atmosphericTick"), null)
                                        )
                                        .then(argument("targets", EntityArgumentType.entities())
                                                .executes(context ->
                                                        AcidCommands.setAtmosphericAcidTick(context, IntegerArgumentType.getInteger(context, "atmosphericTick"), EntityArgumentType.getEntities(context, "targets"))
                                                )
                                        )
                                )
                        )
                        .then(literal("setImmune")
                                .then(argument("value", BoolArgumentType.bool())
                                        .executes(context ->
                                                AcidCommands.setImmunity(context, BoolArgumentType.getBool(context, "value"), null)
                                        )
                                        .then(argument("targets", EntityArgumentType.entities())
                                                .executes(context ->
                                                        AcidCommands.setImmunity(
                                                                context,
                                                                BoolArgumentType.getBool(context, "value"),
                                                                EntityArgumentType.getEntities(context, "targets")
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(literal("item")
                        .then(literal("setMainHand")
                                .then(literal("clearOnConsumption")
                                        .then(argument("value", BoolArgumentType.bool())
                                                .executes(context ->
                                                        AcidCommands.setItemData(context, BoolArgumentType.getBool(context, "value"))
                                                )
                                        )
                                )
                        )
                );
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(acidSubCommand.build());
    }

    private static int setAtmosphericAcidTick(CommandContext<ServerCommandSource> context, int tick, @Nullable Collection<? extends Entity> targets) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<LivingEntity> validTargets = new ArrayList<>();
        if (targets == null) {
            ServerPlayerEntity player = source.getPlayer();
            if (player != null) validTargets.add(player);
        } else {
            for (Entity target : targets) {
                if (!(target instanceof LivingEntity livingEntity)) continue;
                validTargets.add(livingEntity);
            }
        }
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        for (LivingEntity validTarget : validTargets) {
            AcidEntityComponent component = AcidEntityComponent.get(validTarget);
            component.setAcidTicks(tick);
            int newAcidTick = component.getAcidTicks();
            context.getSource().sendFeedback(
                    () -> Text.literal("Set Atmospheric Acid Tick for %s to %s".formatted(validTarget.getName().getString(), newAcidTick)),
                    true
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setImmunity(CommandContext<ServerCommandSource> context, boolean value, @Nullable Collection<? extends Entity> targets) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<LivingEntity> validTargets = new ArrayList<>();
        if (targets == null) {
            ServerPlayerEntity player = source.getPlayer();
            if (player != null) validTargets.add(player);
        } else {
            for (Entity target : targets) {
                if (!(target instanceof LivingEntity livingEntity)) continue;
                validTargets.add(livingEntity);
            }
        }
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        for (LivingEntity validTarget : validTargets) {
            AcidEntityComponent component = AcidEntityComponent.get(validTarget);
            component.setImmune(value);
        }
        context.getSource().sendFeedback(() -> Text.literal("Set Acid Immunity for targets: " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setItemData(CommandContext<ServerCommandSource> context, boolean value) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) throw NO_TARGETS.create();
        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack.isEmpty()) throw NO_TARGETS.create();
        mainHandStack.getOrCreateNbt().putBoolean(NeMuelchNbtKeys.ACID_CLEARER_NBT_KEY, value);
        context.getSource().sendFeedback(() -> Text.literal("Set Main Hand ItemStack to clear acid on consumption: " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<ServerCommandSource> context, @Nullable Collection<? extends Entity> targets) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        List<LivingEntity> validTargets = new ArrayList<>();
        if (targets == null) {
            ServerPlayerEntity player = source.getPlayer();
            if (player != null) validTargets.add(player);
        } else {
            for (Entity target : targets) {
                if (!(target instanceof LivingEntity livingEntity)) continue;
                validTargets.add(livingEntity);
            }
        }
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        source.sendFeedback(() -> Text.literal("Atmospheric Acid values:"), true);
        for (LivingEntity validTarget : validTargets) {
            AcidEntityComponent component = AcidEntityComponent.get(validTarget);
            MutableText output = Text.literal(validTarget.getName().getString())
                    .append(": %s / %s ticks".formatted(component.getAcidTicks(), component.getMaxAcidTicks()));
            source.sendFeedback(() -> output, true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearAcid(CommandContext<ServerCommandSource> context, @Nullable Collection<? extends Entity> targets) throws CommandSyntaxException {
        List<LivingEntity> validTargets = new ArrayList<>();
        if (targets == null) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) validTargets.add(player);
        } else {
            for (Entity target : targets) {
                if (!(target instanceof LivingEntity livingEntity)) continue;
                validTargets.add(livingEntity);
            }
        }
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        for (LivingEntity validTarget : validTargets) {
            AcidEntityComponent component = AcidEntityComponent.get(validTarget);
            component.setAcidTicks(0);
            validTarget.removeStatusEffect(NeMuelchStatusEffects.ACID_BURN);
        }
        context.getSource().sendFeedback(() -> Text.literal("Cleared Atmospheric Acid and Status Effect for targets"), true);
        return Command.SINGLE_SUCCESS;
    }
}
