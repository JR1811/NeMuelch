package net.shirojr.nemuelch.event.handler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.shirojr.nemuelch.compat.cca.implementation.NotificationZoneComponent;
import net.shirojr.nemuelch.compat.cca.util.ComplexZone;
import net.shirojr.nemuelch.event.custom.NotificationZoneCallbacks;

public class NotificationZoneEvents implements NotificationZoneCallbacks.EnteredZone, NotificationZoneCallbacks.LeftZone {
    @Override
    public void onZoneEntered(NotificationZoneComponent component, ComplexZone zone, LivingEntity entity) {
        if (!(component.getWorld() instanceof ServerWorld serverWorld)) return;
        zone.getListeners().forEach((uuid, soundEvent) -> {
            if (!(serverWorld.getEntity(uuid) instanceof ServerPlayerEntity listener)) return;
            this.sendOverlayAndChatMessage(listener, Text.translatable(
                    "info.nemuelch.notification_zone.enter",
                    entity.getName().getString(),
                    zone.getIdentifier().toString()
            ));
            if (soundEvent == null) return;
            listener.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(soundEvent),
                            SoundCategory.MASTER,
                            listener.getX(), listener.getY(), listener.getZ(),
                            2f, 1f,
                            listener.getBlockPos().asLong()
                    )
            );
        });
    }

    @Override
    public void onZoneLeft(NotificationZoneComponent component, ComplexZone zone, LivingEntity entity) {
        if (!(component.getWorld() instanceof ServerWorld serverWorld)) return;
        zone.getListeners().forEach((uuid, soundEvent) -> {
            if (!(serverWorld.getEntity(uuid) instanceof ServerPlayerEntity listener)) return;
            this.sendOverlayAndChatMessage(listener, Text.translatable(
                    "info.nemuelch.notification_zone.left",
                    entity.getName().getString(),
                    zone.getIdentifier().toString()
            ));
            if (soundEvent == null) return;
            listener.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(soundEvent),
                            SoundCategory.MASTER,
                            listener.getX(), listener.getY(), listener.getZ(),
                            2f, 1f,
                            listener.getBlockPos().asLong()
                    )
            );
        });
    }

    private void sendOverlayAndChatMessage(ServerPlayerEntity player, Text text) {
        player.sendMessage(text);
        player.sendMessage(text, true);
    }
}
