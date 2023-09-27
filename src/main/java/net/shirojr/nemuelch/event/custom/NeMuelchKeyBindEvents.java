package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.nemuelch.network.NeMuelchC2SPacketHandler;

public class NeMuelchKeyBindEvents {
    private static final String NEMUELCH_KEYBIND_GROUP = "key.nemuelch.group";
    private static KeyBinding KNOCK_KEY_BIND;

    public static void register() {
        KNOCK_KEY_BIND = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.nemuelch.entry.knocking",
                        InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_L, NEMUELCH_KEYBIND_GROUP)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KNOCK_KEY_BIND.wasPressed() && client.player != null) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeUuid(client.player.getUuid());
                ClientPlayNetworking.send(NeMuelchC2SPacketHandler.KOCKING_SOUND_CHANNEL, buf);
            }
        });
    }

}
