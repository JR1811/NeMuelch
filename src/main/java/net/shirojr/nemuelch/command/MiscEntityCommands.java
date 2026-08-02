package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.misc.EntitySlowingFeature;
import net.shirojr.nemuelch.mixin.access.MobEntityAccess;
import net.shirojr.nemuelch.util.duck.MobPersistency;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MiscEntityCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_TARGETS =
            new SimpleCommandExceptionType(Text.literal("No Targets found"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(literal("entity").requires(source -> source.hasPermissionLevel(2))
                .then(literal("persistent")
                        .then(literal("set")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .then(argument("value", BoolArgumentType.bool())
                                                .executes(MiscEntityCommands::setPersistent)
                                        )
                                )
                        )
                        .then(literal("info")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .executes(MiscEntityCommands::isPersistent)
                                )
                        )
                )
                .then(literal("health")
                        .then(literal("info")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .executes(MiscEntityCommands::printEntityHealth)
                                )
                        )
                        .then(literal("set")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .then(argument("health", FloatArgumentType.floatArg(0))
                                                .executes(MiscEntityCommands::setEntityHealth)
                                        )
                                )
                        )
                )
                .then(literal("nourishment")
                        .then(literal("info")
                                .then(argument("target", EntityArgumentType.player())
                                        .executes(MiscEntityCommands::printNourishment)
                                )
                        )
                        .then(literal("set")
                                .then(literal("hunger")
                                        .then(argument("value", IntegerArgumentType.integer(0, 20))
                                                .then(argument("targets", EntityArgumentType.players())
                                                        .executes(MiscEntityCommands::setHunger)
                                                )
                                        )
                                )
                                .then(literal("saturation")
                                        .then(argument("value", FloatArgumentType.floatArg(0, 20))
                                                .then(argument("targets", EntityArgumentType.players())
                                                        .executes(MiscEntityCommands::setSaturation)
                                                )
                                        )
                                )
                        )
                )
                .then(literal("swap")
                        .then(argument("targetA", EntityArgumentType.entity())
                                .then(argument("targetB", EntityArgumentType.entity())
                                        .executes(MiscEntityCommands::swapEntities)
                                )
                        )
                )
                .then(literal("fire")
                        .then(argument("targets", EntityArgumentType.entities())
                                .then(argument("ticks", IntegerArgumentType.integer(0))
                                        .executes(MiscEntityCommands::setOnFire)
                                )
                        )
                )
                .then(literal("speed")
                        .then(literal("setLimiterLock")
                                .then(argument("locked", BoolArgumentType.bool())
                                        .executes(context -> MiscEntityCommands.setSpeedLimitLock(context, null))
                                        .then(argument("targets", EntityArgumentType.entities())
                                                .executes(context -> MiscEntityCommands.setSpeedLimitLock(context, EntityArgumentType.getEntities(context, "targets")))
                                        )
                                )
                        )
                        .then(literal("setLimiter")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .then(argument("amount", DoubleArgumentType.doubleArg(0, 1))
                                                .executes(context -> MiscEntityCommands.limitSpeed(context, null))
                                                .then(argument("lockLimiter", BoolArgumentType.bool())
                                                        .executes(context -> MiscEntityCommands.limitSpeed(context, BoolArgumentType.getBool(context, "lockLimiter")))
                                                )
                                        )
                                )
                        )
                )
                .then(literal("aggro")
                        .then(literal("clear")
                                .then(argument("targets", EntityArgumentType.entities())
                                        .executes(MiscEntityCommands::clearAggro)
                                )
                        )
                )
        );
    }

    private static int clearAggro(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
        for (Entity target : targets) {
            if (target instanceof Angerable angerable && angerable.getAngryAt() != null) {
                angerable.setAngryAt(null);
                context.getSource().sendFeedback(() -> Text.literal("Cleared Aggro of " + target.getName().getString()), true);
            }
            if (target instanceof MobEntity mobEntity) {
                mobEntity.setTarget(null);
            }
            if (target instanceof MobEntityAccess mobEntityAccess) {
                mobEntityAccess.getGoalSelector().getRunningGoals()
                        .filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof TrackTargetGoal)
                        .map(prioritizedGoal -> ((TrackTargetGoal) prioritizedGoal.getGoal()))
                        .forEach(TrackTargetGoal::stop);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int isPersistent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<MobEntity> mobTargets = new ArrayList<>();
        EntityArgumentType.getEntities(context, "targets").forEach(entity -> {
            if (entity instanceof MobEntity mobTarget) {
                mobTargets.add(mobTarget);
            }
        });
        if (mobTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        for (MobEntity mobTarget : mobTargets) {
            if (!(mobTarget instanceof MobPersistency mobPersistency)) continue;
            context.getSource().sendFeedback(() ->
                            Text.literal(mobTarget.getName().getString()).append(Text.literal(": ")
                                    .append(String.valueOf(mobPersistency.nemuelch$isPersistent()))),
                    true
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setPersistent(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<MobPersistency> mobTargets = new ArrayList<>();
        EntityArgumentType.getEntities(context, "targets").forEach(entity -> {
            if (entity instanceof MobPersistency mobTarget) {
                mobTargets.add(mobTarget);
            }
        });
        if (mobTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        boolean persistent = BoolArgumentType.getBool(context, "value");
        mobTargets.forEach(mobEntity -> mobEntity.nemuelch$setPersistent(persistent));
        context.getSource().sendFeedback(() -> Text.literal("Set persistency to %s for applicable targets".formatted(persistent)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setSpeedLimitLock(CommandContext<ServerCommandSource> context, @Nullable Collection<? extends Entity> targets) throws CommandSyntaxException {
        boolean locked = BoolArgumentType.getBool(context, "locked");
        Collection<LivingEntity> validTargets = new ArrayList<>();
        if (targets == null) {
            ServerPlayerEntity user = context.getSource().getPlayer();
            if (user != null) {
                validTargets.add(user);
            }
        } else {
            targets.forEach(entity -> {
                if (entity instanceof LivingEntity livingEntity) validTargets.add(livingEntity);
            });
        }
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        validTargets.forEach(validTarget -> {
            MiscEntityComponent component = MiscEntityComponent.get(validTarget);
            component.setSlowingLock(locked, true);
        });
        context.getSource().sendFeedback(() -> Text.literal("Changed Speed Limiter Lock: " + locked), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int limitSpeed(CommandContext<ServerCommandSource> context, @Nullable Boolean lockSpeed) throws CommandSyntaxException {
        List<LivingEntity> validTargets = new ArrayList<>();
        EntityArgumentType.getEntities(context, "targets").forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity && EntitySlowingFeature.hasSpeedEntityAttribute(entity)) {
                validTargets.add(livingEntity);
            }
        });
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        double amount = -(1 - DoubleArgumentType.getDouble(context, "amount"));
        for (LivingEntity validTarget : validTargets) {
            EntityAttributeInstance instance = EntitySlowingFeature.getTemporarySpeedAttributeInstance(validTarget);
            if (instance != null) {
                EntitySlowingFeature.setTemporarySpeed(validTarget, instance, operand -> amount, true);
            }
            if (lockSpeed != null) {
                MiscEntityComponent component = MiscEntityComponent.get(validTarget);
                component.setSlowingLock(lockSpeed, true);
            }
        }
        context.getSource().sendFeedback(() -> {
            MutableText line = Text.empty();
            line.append(Text.literal("Speed: %s%%".formatted(EntitySlowingFeature.asPercentage(amount))));
            return line;
        }, true);
        if (lockSpeed != null) {
            context.getSource().sendFeedback(() -> {
                MutableText line = Text.empty();
                line.append(Text.literal("Set Attribute Modifier lock: ").formatted(Formatting.WHITE));
                line.append(Text.literal(String.valueOf(lockSpeed)).formatted(Formatting.GRAY));
                return line;
            }, true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setHunger(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int newHunger = IntegerArgumentType.getInteger(context, "value");
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            player.getHungerManager().setFoodLevel(newHunger);
            String playerName = player.getName().getString();
            context.getSource().sendFeedback(() -> Text.literal("Set %s's Food Level: %s/20".formatted(playerName, newHunger)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setSaturation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        float newSaturation = FloatArgumentType.getFloat(context, "value");
        for (ServerPlayerEntity player : EntityArgumentType.getPlayers(context, "targets")) {
            player.getHungerManager().setSaturationLevel(newSaturation);
            String playerName = player.getName().getString();
            context.getSource().sendFeedback(() -> Text.literal("Set %s's Saturation Level: %s/20".formatted(playerName, newSaturation)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int printNourishment(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
        String playerName = player.getName().getString();
        HungerManager hungerManager = player.getHungerManager();
        context.getSource().sendFeedback(() ->
                Text.literal(playerName + "'s Food Level: %s/20".formatted(hungerManager.getFoodLevel())), true);
        context.getSource().sendFeedback(() ->
                Text.literal(playerName + "'s Saturation Level: %s/20".formatted(hungerManager.getSaturationLevel())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int printEntityHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<LivingEntity> validTargets = new ArrayList<>();
        EntityArgumentType.getEntities(context, "targets").forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) validTargets.add(livingEntity);
        });
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        for (Entity entity : validTargets) {
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            float health = livingEntity.getHealth();
            float maxHealth = livingEntity.getMaxHealth();
            MutableText feedback = Text.empty();
            feedback.append(Text.literal("[%s] ".formatted(livingEntity.getName().getString())).formatted(Formatting.GREEN));
            feedback.append(Text.literal("Health: %s/%s".formatted(health, maxHealth)));
            context.getSource().sendFeedback(() -> feedback, true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int setEntityHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<LivingEntity> validTargets = new ArrayList<>();
        EntityArgumentType.getEntities(context, "targets").forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) validTargets.add(livingEntity);
        });
        if (validTargets.isEmpty()) {
            throw NO_TARGETS.create();
        }
        float health = FloatArgumentType.getFloat(context, "health");
        for (LivingEntity validTarget : validTargets) {
            float newHealth = Math.min(health, validTarget.getMaxHealth());
            validTarget.setHealth(newHealth);
            String line = "§6[%s]§r New Health: %s".formatted(validTarget.getName().getString(), newHealth);
            context.getSource().sendFeedback(() -> Text.literal(line).formatted(Formatting.ITALIC), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int swapEntities(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();

        Entity entityA = EntityArgumentType.getEntity(context, "targetA");
        Entity entityB = EntityArgumentType.getEntity(context, "targetB");

        Vec3d posA = entityA.getPos();
        Vec3d posB = entityB.getPos();

        Vec3d velocityA = entityA.getVelocity();
        Vec3d velocityB = entityA.getVelocity();

        float pitchA = entityA.getPitch();
        float pitchB = entityB.getPitch();
        float yawA = entityA.getYaw();
        float yawB = entityB.getYaw();

        Set<PositionFlag> positionFlags = EnumSet.allOf(PositionFlag.class);
        entityA.teleport(world, posB.x, posB.y, posB.z, positionFlags, yawB, pitchB);
        entityB.teleport(world, posA.x, posA.y, posA.z, positionFlags, yawA, pitchA);

        if (entityA instanceof PathAwareEntity pathAwareA) pathAwareA.getNavigation().stop();
        if (entityB instanceof PathAwareEntity pathAwareB) pathAwareB.getNavigation().stop();

        entityA.setVelocity(velocityB);
        entityB.setVelocity(velocityA);

        String line = "Swapped §6[%s]§r with §6[%s]§r".formatted(entityA.getName().getString(), entityB.getName().getString());
        context.getSource().sendFeedback(() -> Text.literal(line).formatted(Formatting.ITALIC), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setOnFire(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
        if (targets.isEmpty()) throw NO_TARGETS.create();
        int ticks = IntegerArgumentType.getInteger(context, "ticks");

        for (Entity target : targets) {
            String line = "Set §6[%s]§r on fire for %s ticks".formatted(target.getName().getString(), ticks);
            context.getSource().sendFeedback(() -> Text.literal(line).formatted(Formatting.ITALIC), true);
            target.setFireTicks(ticks);
        }
        return Command.SINGLE_SUCCESS;
    }
}
