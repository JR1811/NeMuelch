package net.shirojr.nemuelch.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.shirojr.nemuelch.compat.timewind.Phase;

public class PhaseArgumentType extends EnumArgumentType<Phase> {
    private PhaseArgumentType() {
        super(Phase.CODEC, Phase::values);
    }

    public static EnumArgumentType<Phase> phase() {
        return new PhaseArgumentType();
    }

    public static Phase getPhase(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, Phase.class);
    }
}
