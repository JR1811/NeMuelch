package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.sound.SoundData;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MiscSoundCommands implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType MISSING_SOURCES =
            new SimpleCommandExceptionType(Text.literal("No valid sources to follow specified"));
    private static final SimpleCommandExceptionType MISSING_LISTENERS =
            new SimpleCommandExceptionType(Text.literal("No valid listeners specified"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> commandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        RequiredArgumentBuilder<ServerCommandSource, Identifier> soundArgument = CommandManager.argument("sound", IdentifierArgumentType.identifier())
                .suggests(SuggestionProviders.AVAILABLE_SOUNDS);

        LiteralCommandNode<ServerCommandSource> rootNode = commandDispatcher.register(literal("playfollowingsound").requires(source -> source.hasPermissionLevel(2)));
        for (SoundCategory category : SoundCategory.values()) {
            soundArgument.then(literal(category.getName())
                    .executes(context -> MiscSoundCommands.startFollowingSound(context, category, false, false, false))
                    .then(argument("volume", FloatArgumentType.floatArg())
                            .executes(context -> MiscSoundCommands.startFollowingSound(context, category, true, false, false))
                            .then(argument("pitch", FloatArgumentType.floatArg())
                                    .executes(context -> MiscSoundCommands.startFollowingSound(context, category, true, true, false))
                                    .then(argument("repeat", IntegerArgumentType.integer(1))
                                            .executes(context -> MiscSoundCommands.startFollowingSound(context, category, true, true, true))
                                    )
                            )
                    )
            );
        }
        rootNode.addChild(argument("sources", EntityArgumentType.entities())
                .then(literal("for")
                        .then(argument("listeners", EntityArgumentType.players())
                                .then(soundArgument)
                        )
                )
                .build()
        );

        commandDispatcher.register(literal("stopfollowingsound")
                .then(argument("sound", IdentifierArgumentType.identifier()).suggests(SuggestionProviders.AVAILABLE_SOUNDS)
                        .then(argument("listeners", EntityArgumentType.players())
                                .executes(MiscSoundCommands::stopFollowingSound)
                        )
                )
        );
    }

    private static int stopFollowingSound(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");
        Identifier soundId = IdentifierArgumentType.getIdentifier(context, "sound");
        StringBuilder nameCollector = new StringBuilder();
        for (ServerPlayerEntity listener : listeners) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(soundId);
            ServerPlayNetworking.send(listener, NetworkIdentifiers.STOP_FOLLOWING_SOUND_INSTANCE, buf);
            if (!nameCollector.isEmpty()) nameCollector.append(", ");
            nameCollector.append(listener.getName().getString());
        }
        context.getSource().sendFeedback(() -> Text.literal("Stopped SoundInstances with the sound of %s for %s".formatted(soundId, nameCollector.toString())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int startFollowingSound(CommandContext<ServerCommandSource> context, SoundCategory category, boolean hasVolume, boolean hasPitch, boolean hasRepeat) throws CommandSyntaxException {
        Collection<? extends Entity> sources = EntityArgumentType.getEntities(context, "sources");
        Collection<ServerPlayerEntity> listeners = EntityArgumentType.getPlayers(context, "listeners");

        if (sources.isEmpty()) {
            throw MISSING_SOURCES.create();
        }
        if (listeners.isEmpty()) {
            throw MISSING_LISTENERS.create();
        }

        SoundEvent sound = SoundEvent.of(IdentifierArgumentType.getIdentifier(context, "sound"));
        float volume = 1f;
        float pitch = 1f;
        int repeat = 0;
        if (hasVolume) {
            volume = FloatArgumentType.getFloat(context, "volume");
            if (hasPitch) {
                pitch = FloatArgumentType.getFloat(context, "pitch");
                if (hasRepeat) {
                    repeat = IntegerArgumentType.getInteger(context, "repeat");
                }
            }
        }
        SoundData soundData = new SoundData(sound, category, volume, pitch, repeat);

        StringBuilder listenerNameCollector = new StringBuilder();
        StringBuilder sourceNameCollector = new StringBuilder();
        for (ServerPlayerEntity packetTarget : listeners) {
            for (Entity source : sources) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(source.getId());
                soundData.toPacketByteBuf(buf);
                ServerPlayNetworking.send(packetTarget, NetworkIdentifiers.START_FOLLOWING_SOUND_INSTANCE, buf);
                if (!sourceNameCollector.isEmpty()) sourceNameCollector.append(", ");
                sourceNameCollector.append(source.getName().getString());
            }

            if (!listenerNameCollector.isEmpty()) listenerNameCollector.append(", ");
            listenerNameCollector.append(packetTarget.getName().getString());
        }
        context.getSource().sendFeedback(() -> Text.literal("Playing SoundInstance %s which will follow %s for %s".formatted(sound.getId().toString(), sourceNameCollector, listenerNameCollector)), true);
        return Command.SINGLE_SUCCESS;
    }
}
