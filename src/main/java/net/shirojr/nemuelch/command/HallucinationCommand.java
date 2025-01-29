package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.shirojr.nemuelch.util.wrapper.Illusionable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * <h1>Hallucination</h1>
 * A target entity can only be rendered by specific other entities. <br><br>
 * <ul>
 *     <li>illusion: the entity which is <b>not</b> visible to all entities</li>
 *     <li>victim: the entity which is is allowed to render the illusion</li>
 * </ul>
 */
public class HallucinationCommand {
    public static final String ILLUSION_KEY = "illusion", ILLUSION_STATE_KEY = "illusionState", VICTIMS_KEY = "victims";

    private static final SimpleCommandExceptionType NOT_ILLUSIONABLE =
            new SimpleCommandExceptionType(new LiteralText("Entity can't be an illusion"));
    private static final SimpleCommandExceptionType NO_VICTIMS_AVAILABLE =
            new SimpleCommandExceptionType(new LiteralText("No entries in victims list were applicable"));

    @SuppressWarnings("unused")
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("nemuelch").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                .then(literal("hallucinate")
                        .then(literal("set")
                                .then(argument(ILLUSION_KEY, EntityArgumentType.entities())
                                        .then(argument(ILLUSION_STATE_KEY, BoolArgumentType.bool())
                                                .executes(HallucinationCommand::setHallucinationSate))))
                        .then(literal("add")
                                .then(argument(ILLUSION_KEY, EntityArgumentType.entities())
                                        .then(argument(VICTIMS_KEY, EntityArgumentType.entities())
                                                .executes(HallucinationCommand::addHallucinationTargets))))
                        .then(literal("remove")
                                .then(argument(ILLUSION_KEY, EntityArgumentType.entities())
                                        .executes(HallucinationCommand::clearAllHallucinationTargets)
                                        .then(argument(VICTIMS_KEY, EntityArgumentType.entities())
                                                .executes(HallucinationCommand::clearHallucinationTargets))))));
    }

    private static int setHallucinationSate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<Entity> illusions = new ArrayList<>(EntityArgumentType.getEntities(context, ILLUSION_KEY));
        boolean isIllusion = BoolArgumentType.getBool(context, ILLUSION_STATE_KEY);
        for (Entity illusion : illusions) {
            if (!(illusion instanceof Illusionable illusionRendering)) throw NOT_ILLUSIONABLE.create();
            illusionRendering.nemuelch$setIllusion(isIllusion);
            if (!isIllusion) illusionRendering.nemuelch$clearIllusionTargets();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addHallucinationTargets(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<Entity> illusions = new ArrayList<>(EntityArgumentType.getEntities(context, ILLUSION_KEY));
        List<Entity> victims = new ArrayList<>(EntityArgumentType.getEntities(context, VICTIMS_KEY));
        if (victims.isEmpty()) throw NO_VICTIMS_AVAILABLE.create();

        for (Entity illusion : illusions) {
            if (!(illusion instanceof Illusionable illusionRendering)) throw NOT_ILLUSIONABLE.create();
            illusionRendering.nemuelch$modifyIllusionTargets(uuids -> uuids.addAll(victims.stream().map(Entity::getUuid).toList()));

            StringBuilder sb = new StringBuilder("Added");
            victims.forEach(entity -> sb.append(" ").append(entity.getName().getString()));
            context.getSource().sendFeedback(new LiteralText(sb.toString()), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int clearAllHallucinationTargets(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> illusions = EntityArgumentType.getEntities(context, ILLUSION_KEY);
        for (Entity illusion : illusions) {
            if (!(illusion instanceof Illusionable illusionRendering)) throw NOT_ILLUSIONABLE.create();
            illusionRendering.nemuelch$clearIllusionTargets();
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearHallucinationTargets(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<? extends Entity> illusions = EntityArgumentType.getEntities(context, ILLUSION_KEY);
        Collection<? extends Entity> victims = EntityArgumentType.getEntities(context, VICTIMS_KEY);
        for (Entity illusion : illusions) {
            if (!(illusion instanceof Illusionable illusionRendering)) throw NOT_ILLUSIONABLE.create();
            illusionRendering.nemuelch$modifyIllusionTargets(uuids -> uuids.removeAll(victims.stream().map(Entity::getUuid).toList()));
        }
        return Command.SINGLE_SUCCESS;
    }

}
