package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.component.GeneralMonsterComponent;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.type.DryadMonsterType;
import net.shirojr.nemuelch.monster.type.HumanMonsterType;
import net.shirojr.nemuelch.monster.type.VampireMonsterType;
import net.shirojr.nemuelch.monster.type.WerwolfMonsterType;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MonsterCommands implements CommandRegistrationCallback {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher,
                         CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        commandDispatcher.register(literal("monster").requires(source -> source.hasPermissionLevel(2))
                .then(literal("set")
                        .then(argument("vampire", FloatArgumentType.floatArg(0, 1))
                                .then(argument("dryad", FloatArgumentType.floatArg(0, 1))
                                        .then(argument("werwolf", FloatArgumentType.floatArg(0, 1))
                                                .then(argument("human", FloatArgumentType.floatArg(0, 1))
                                                        .executes(MonsterCommands::setMonsterValues)
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setMonsterValues(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }

        float vampire = FloatArgumentType.getFloat(context, "vampire");
        float dryad = FloatArgumentType.getFloat(context, "dryad");
        float werwolf = FloatArgumentType.getFloat(context, "werwolf");
        float human = FloatArgumentType.getFloat(context, "human");

        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);

        if (vampire == 0 && dryad == 0 && werwolf == 0 && human == 0) {
            monsterComponent.reset();
        } else {
            monsterComponent.setWithProportions(monsterComponent.getMonsterType(VampireMonsterType.IDENTIFIER), vampire);
            monsterComponent.setWithProportions(monsterComponent.getMonsterType(DryadMonsterType.IDENTIFIER), dryad);
            monsterComponent.setWithProportions(monsterComponent.getMonsterType(WerwolfMonsterType.IDENTIFIER), werwolf);
            monsterComponent.setWithProportions(monsterComponent.getMonsterType(HumanMonsterType.IDENTIFIER), human);
        }
        StringBuilder sb = new StringBuilder("[%s] ".formatted(player.getName().getString()));
        for (AbstractMonsterType activeMonsterType : monsterComponent.getActiveMonsterTypes()) {
            sb.append(activeMonsterType.getIdentifier().getPath())
                    .append(": ")
                    .append(activeMonsterType.getDominance())
                    .append(" | ");
        }
        context.getSource().sendFeedback(() -> Text.literal(sb.toString()), true);
        return Command.SINGLE_SUCCESS;
    }
}