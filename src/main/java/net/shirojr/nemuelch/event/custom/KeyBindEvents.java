package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;
import net.shirojr.nemuelch.util.logger.LoggerUtil;

public class KeyBindEvents {
    private static KeyBinding KNOCK_KEY_BIND;
    private static final String NEMUELCH_KEYBIND_GROUP = "key.nemuelch.group";

    private static boolean wasKnocked = false;

    public static void register() {
        KNOCK_KEY_BIND = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.nemuelch.entry.knocking",
                        InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_L, NEMUELCH_KEYBIND_GROUP)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (!KNOCK_KEY_BIND.isPressed() && wasKnocked) {
                wasKnocked = false;
            }
            else if (KNOCK_KEY_BIND.isPressed() && !wasKnocked) {
                HitResult hitResult = client.player.raycast(NeMuelchConfigInit.CONFIG.knockableBlockRange, client.getTickDelta(), false);
                if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
                wasKnocked = true;
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(blockHitResult.getBlockPos());
                ClientPlayNetworking.send(NetworkIdentifiers.KNOCKING_RAYCASTED_SOUND_C2S, buf);
                LoggerUtil.devLogger("Raycast: " + client.player.getWorld().getBlockState(BlockPos.ofFloored(hitResult.getPos())));
            }
        });
    }

}
