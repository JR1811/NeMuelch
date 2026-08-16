package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.network.packet.MonsterAbilityKeyPressC2SPacket;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.util.logger.LoggerUtil;

import java.util.function.Consumer;

public class KeyBindEvents implements ClientTickEvents.EndTick {
    private static final String NEMUELCH_KEYBIND_GROUP = "key.nemuelch.group";


    private static final KeyBinding KNOCK_KEY_BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nemuelch.entry.knocking",
                    InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_L, NEMUELCH_KEYBIND_GROUP)
    );
    public static final KeyBinding SLOWING_KEY_BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nemuelch.entry.slowing",
                    InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_RIGHT_SHIFT, NEMUELCH_KEYBIND_GROUP)
    );
    private static final KeyBinding MONSTER_ABILITY_0_KEY_BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nemuelch.entry.monster_1",
                    InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), NEMUELCH_KEYBIND_GROUP)
    );
    private static final KeyBinding MONSTER_ABILITY_1_KEY_BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nemuelch.entry.monster_2",
                    InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), NEMUELCH_KEYBIND_GROUP)
    );
    private static final KeyBinding MONSTER_ABILITY_2_KEY_BIND = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.nemuelch.entry.monster_3",
                    InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), NEMUELCH_KEYBIND_GROUP)
    );


    private static boolean wasKnocked = false;
    private static boolean ability1 = false;
    private static boolean ability2 = false;
    private static boolean ability3 = false;
    public static boolean pressedSlowing = false;

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.player == null) return;

        if (!KNOCK_KEY_BIND.isPressed() && wasKnocked) {
            wasKnocked = false;
        } else if (KNOCK_KEY_BIND.isPressed() && !wasKnocked) {
            HitResult hitResult = client.player.raycast(NeMuelchConfigInit.CONFIG.knockableBlockRange, client.getTickDelta(), false);
            if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
            wasKnocked = true;
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(blockHitResult.getBlockPos());
            ClientPlayNetworking.send(NetworkIdentifiers.KNOCKING_RAYCASTED_SOUND_C2S, buf);
            LoggerUtil.devLogger("Raycast: " + client.player.getWorld().getBlockState(BlockPos.ofFloored(hitResult.getPos())));
        }

        if (!SLOWING_KEY_BIND.isPressed() && pressedSlowing) {
            pressedSlowing = false;
        } else if (SLOWING_KEY_BIND.isPressed() && !pressedSlowing) {
            pressedSlowing = true;
        }

        handleRisingEdge(MONSTER_ABILITY_0_KEY_BIND, ability1, aBoolean -> ability1 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(0, true).send()
        );
        handleRisingEdge(MONSTER_ABILITY_1_KEY_BIND, ability2, aBoolean -> ability2 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(1, true).send()
        );
        handleRisingEdge(MONSTER_ABILITY_2_KEY_BIND, ability3, aBoolean -> ability3 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(2, true).send()
        );

        handleFallingEdge(MONSTER_ABILITY_0_KEY_BIND, ability1, aBoolean -> ability1 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(0, false).send()
        );
        handleFallingEdge(MONSTER_ABILITY_1_KEY_BIND, ability2, aBoolean -> ability2 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(1, false).send()
        );
        handleFallingEdge(MONSTER_ABILITY_2_KEY_BIND, ability3, aBoolean -> ability3 = aBoolean, () ->
                new MonsterAbilityKeyPressC2SPacket(2, false).send()
        );
    }

    private static void handleRisingEdge(KeyBinding key, boolean keyBuffer, Consumer<Boolean> keyBufferSetter, Runnable runnable) {
        if (!key.isPressed() && keyBuffer) {
            keyBufferSetter.accept(false);
        } else if (key.isPressed() && !keyBuffer) {
            runnable.run();
        }
    }

    private static void handleFallingEdge(KeyBinding key, boolean keyBuffer, Consumer<Boolean> keyBufferSetter, Runnable runnable) {
        if (key.isPressed() && !keyBuffer) {
            keyBufferSetter.accept(true);
        } else if (!key.isPressed() && keyBuffer) {
            runnable.run();
        }
    }
}
