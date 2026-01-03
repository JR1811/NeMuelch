package net.shirojr.nemuelch.command.argument;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.shirojr.nemuelch.compat.satin.util.NetworkingParameter;
import net.shirojr.nemuelch.compat.timewind.Phase;

public class ShaderNetworkingParameterArgumentType extends EnumArgumentType<NetworkingParameter> {
    private ShaderNetworkingParameterArgumentType() {
        super(NetworkingParameter.CODEC, NetworkingParameter::values);
    }

    public static EnumArgumentType<NetworkingParameter> parameter() {
        return new ShaderNetworkingParameterArgumentType();
    }

    public static NetworkingParameter getParameter(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, NetworkingParameter.class);
    }
}
