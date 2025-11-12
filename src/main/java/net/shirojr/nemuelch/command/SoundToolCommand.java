package net.shirojr.nemuelch.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.init.NeMuelchItems;
import net.shirojr.nemuelch.item.custom.adminToolItem.SoundToolItem;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SoundToolCommand implements CommandRegistrationCallback {
    private static final SimpleCommandExceptionType NO_USER =
            new SimpleCommandExceptionType(Text.literal("Command needs to be executed by an Entity"));
    private static final SimpleCommandExceptionType NOT_VALID_SOUND =
            new SimpleCommandExceptionType(Text.literal("Specified SoundEvent is not valid"));

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> soundToolCommandNode = literal("sound").then(literal("tool")
                .then(literal("create")
                        .then(argument("sound", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.SOUND_EVENT))
                                .executes(SoundToolCommand::run)
                                .then(argument("volume", FloatArgumentType.floatArg())
                                        .then(argument("pitch", FloatArgumentType.floatArg())
                                                .executes(SoundToolCommand::runVolPitch)
                                                .then(argument("pos", Vec3ArgumentType.vec3())
                                                        .executes(SoundToolCommand::runVolPitchPos)
                                                )
                                                .then(argument("target", EntityArgumentType.entity())
                                                        .executes(SoundToolCommand::runVolPitchTarget)
                                                )
                                        )
                                )
                        )
                )
                /*.then(literal("add")
                        .then(argument("sound", RegistryEntryArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.SOUND_EVENT))
                                .executes(SoundToolCommand::runAddSound)
                        )
                )*/
        );
        NeMuelchCommandUtil.getOrCreateNeMuelchNode(dispatcher).addChild(soundToolCommandNode.build());
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw NO_USER.create();
        }
        RegistryKey<SoundEvent> sound = RegistryEntryArgumentType.getRegistryEntry(context, "sound", RegistryKeys.SOUND_EVENT).getKey().orElseThrow(NOT_VALID_SOUND::create);
        ItemStack stack = NeMuelchItems.SOUND_TOOL.getDefaultStack();
        modifyStack(stack, Registries.SOUND_EVENT.get(sound), null, null, null, null);
        player.getInventory().offerOrDrop(stack);
        context.getSource().sendFeedback(() -> Text.literal("Created new Item successfully"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int runVolPitch(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw NO_USER.create();
        }
        RegistryKey<SoundEvent> sound = RegistryEntryArgumentType.getRegistryEntry(context, "sound", RegistryKeys.SOUND_EVENT).getKey().orElseThrow(NOT_VALID_SOUND::create);
        ItemStack stack = NeMuelchItems.SOUND_TOOL.getDefaultStack();
        float volume = FloatArgumentType.getFloat(context, "volume");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        modifyStack(stack, Registries.SOUND_EVENT.get(sound), volume, pitch, null, null);
        player.getInventory().offerOrDrop(stack);
        context.getSource().sendFeedback(() -> Text.literal("Created new Item successfully"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int runVolPitchPos(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw NO_USER.create();
        }
        RegistryKey<SoundEvent> sound = RegistryEntryArgumentType.getRegistryEntry(context, "sound", RegistryKeys.SOUND_EVENT).getKey().orElseThrow(NOT_VALID_SOUND::create);
        ItemStack stack = NeMuelchItems.SOUND_TOOL.getDefaultStack();
        float volume = FloatArgumentType.getFloat(context, "volume");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        Vec3d pos = Vec3ArgumentType.getVec3(context, "pos");
        modifyStack(stack, Registries.SOUND_EVENT.get(sound), volume, pitch, pos, null);
        player.getInventory().offerOrDrop(stack);
        context.getSource().sendFeedback(() -> Text.literal("Created new Item successfully"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int runVolPitchTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            throw NO_USER.create();
        }
        RegistryKey<SoundEvent> sound = RegistryEntryArgumentType.getRegistryEntry(context, "sound", RegistryKeys.SOUND_EVENT).getKey().orElseThrow(NOT_VALID_SOUND::create);
        ItemStack stack = NeMuelchItems.SOUND_TOOL.getDefaultStack();
        float volume = FloatArgumentType.getFloat(context, "volume");
        float pitch = FloatArgumentType.getFloat(context, "pitch");
        Entity entity = EntityArgumentType.getEntity(context, "target");
        modifyStack(stack, Registries.SOUND_EVENT.get(sound), volume, pitch, null, entity);
        player.getInventory().offerOrDrop(stack);
        context.getSource().sendFeedback(() -> Text.literal("Created new Item successfully"), true);
        return Command.SINGLE_SUCCESS;
    }


    private static void modifyStack(ItemStack stack, SoundEvent sound, @Nullable Float volume, @Nullable Float pitch, @Nullable Vec3d pos, @Nullable Entity target) {
        SoundToolItem.setSound(stack, sound);

        if (volume != null) {
            SoundToolItem.setVolume(stack, volume);
        }
        if (pitch != null) {
            SoundToolItem.setPitch(stack, pitch);
        }
        if (pos != null) {
            SoundToolItem.setPos(stack, pos);
        }
        if (target != null) {
            SoundToolItem.setTarget(stack, target);
        }
    }
}
