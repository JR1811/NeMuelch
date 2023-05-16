package net.shirojr.nemuelch.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.shirojr.nemuelch.util.BodyParts;
import net.shirojr.nemuelch.util.cast.IBodyPartSaver;

import java.util.Objects;

public class BodyPartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("nemuelch").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("bodyPart")
                        .then(CommandManager.literal("changeEntry")
                                .then(CommandManager.argument("bodyPartName", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (var entry : BodyParts.values()) {
                                                builder.suggest(entry.getBodyPartName());
                                            }
                                            return builder.buildFuture();
                                        })
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(BodyPartCommand::run))))
                        .then(CommandManager.literal("removeAllEntries")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(BodyPartCommand::runRemoval))))
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String bodyPartInput = StringArgumentType.getString(context, "bodyPartName");
        IBodyPartSaver targetPlayer = (IBodyPartSaver) EntityArgumentType.getPlayer(context, "target");
        IBodyPartSaver source = (IBodyPartSaver) context.getSource().getPlayer();

        return targetPlayer.editPersistentData(persistentData -> {
            for (var entry : BodyParts.values()) {
                if (Objects.equals(entry.getBodyPartName(), bodyPartInput)) {
                    if (partExistsInNbt(persistentData, entry)) {
                        persistentData.remove(entry.getBodyPartName());
                        context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.bodypart.removed"), true);
                    } else {
                        persistentData.putString(entry.getBodyPartName(), context.getSource().getName());   //TODO: get a better list of strings nbt structure
                        context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.bodypart.added"), true);
                    }

                    return 1;
                }
            }

            context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.bodypart.error"), true);
            return -1;
        });
    }

    private static boolean partExistsInNbt(NbtCompound nbt, BodyParts entry) {
        return nbt.contains(entry.getBodyPartName());
    }

    private static int runRemoval(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        IBodyPartSaver targetPlayer = (IBodyPartSaver) EntityArgumentType.getPlayer(context, "target");

        return targetPlayer.editPersistentData(persistentData -> {
            for (var entry : BodyParts.values()) {
                if (persistentData.contains(entry.getBodyPartName())) {
                    persistentData.remove(entry.getBodyPartName());
                }
            }
            context.getSource().sendFeedback(new TranslatableText("feedback.nemuelch.bodypart.removed.all"), true);
            return 1;
        });
    }
}
