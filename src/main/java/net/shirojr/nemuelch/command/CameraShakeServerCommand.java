package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.camera.Displacement;
import net.shirojr.nemuelch.camera.Easing;
import net.shirojr.nemuelch.command.argument.EasingArgumentType;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import org.joml.Vector3f;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CameraShakeServerCommand implements CommandRegistrationCallback {

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> subCommand = literal("shake")
                .then(argument("targets", EntityArgumentType.players())
                        .then(argument("duration", IntegerArgumentType.integer(0))
                                .then(argument("finalHoldDuration", IntegerArgumentType.integer(0))
                                        .then(argument("easing", EasingArgumentType.easing())
                                                .then(argument("yaw", FloatArgumentType.floatArg())
                                                        .then(argument("pitch", FloatArgumentType.floatArg())
                                                                .then(argument("roll", FloatArgumentType.floatArg())
                                                                        .executes(context -> CameraShakeServerCommand.handleDisplacement(context, false))
                                                                        .then(argument("posOffset", Vec3ArgumentType.vec3(false))
                                                                                .executes(context -> CameraShakeServerCommand.handleDisplacement(context, true))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(literal("clear")
                                .executes(CameraShakeServerCommand::clearShake)
                        )
                );

        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(subCommand.build());
    }

    private static int clearShake(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        for (ServerPlayerEntity target : targets) {
            ServerPlayNetworking.send(target, NetworkIdentifiers.CLEAR_CAMERA_SHAKE_PACKET, PacketByteBufs.empty());
        }

        context.getSource().sendFeedback(() -> Text.literal("Camera Shake cleared for targets"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleDisplacement(CommandContext<ServerCommandSource> context, boolean includePositionDisplacement) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        int duration = IntegerArgumentType.getInteger(context, "duration");
        int finalHoldDuration = IntegerArgumentType.getInteger(context, "finalHoldDuration");
        Easing easing = EasingArgumentType.getEasing(context, "easing");
        Vector3f rotations = new Vector3f(
                FloatArgumentType.getFloat(context, "yaw"),
                FloatArgumentType.getFloat(context, "pitch"),
                FloatArgumentType.getFloat(context, "roll")
        );

        Displacement targetDisplacement = includePositionDisplacement ?
                new Displacement(Vec3ArgumentType.getVec3(context, "posOffset"), rotations) :
                new Displacement(rotations);

        for (ServerPlayerEntity target : targets) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(duration);
            buf.writeVarInt(finalHoldDuration);
            buf.writeVarInt(easing.ordinal());
            targetDisplacement.toPacketByteBuf(buf);
            ServerPlayNetworking.send(target, NetworkIdentifiers.CAMERA_SHAKE_PACKET, buf);
        }

        context.getSource().sendFeedback(() -> Text.literal("Applied Camera Shake to targets"), true);
        return Command.SINGLE_SUCCESS;
    }
}
