package net.shirojr.nemuelch.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.shirojr.nemuelch.camera.Easing;

public class EasingArgumentType extends EnumArgumentType<Easing> {
    private EasingArgumentType() {
        super(Easing.CODEC, Easing::values);
    }

    public static EnumArgumentType<Easing> easing() {
        return new EasingArgumentType();
    }

    public static Easing getEasing(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, Easing.class);
    }
}
