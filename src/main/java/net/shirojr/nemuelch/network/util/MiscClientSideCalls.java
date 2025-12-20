package net.shirojr.nemuelch.network.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.shirojr.nemuelch.block.entity.custom.AdvancedFogBlockEntity;
import net.shirojr.nemuelch.screen.custom.AdvancedFogScreen;

public class MiscClientSideCalls {
    public static void openAdvancedFogBlockScreen(PlayerEntity player, AdvancedFogBlockEntity blockEntity) {
        if (!(player instanceof ClientPlayerEntity)) {
            throw new IllegalStateException("client only method was accessed on logical server side");
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        client.setScreen(new AdvancedFogScreen(blockEntity));
    }
}
