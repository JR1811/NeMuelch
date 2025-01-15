package net.shirojr.nemuelch.effect.custom;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.PacketByteBuf;
import net.shirojr.nemuelch.util.constants.NetworkIdentifiers;

@SuppressWarnings("CodeBlock2Expr")
public class ObfuscatedEffect extends StatusEffect {
    public ObfuscatedEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.getWorld().isClient() || entity.getServer() == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(entity.getUuid());
        buf.writeBoolean(true);
        PlayerLookup.all(entity.getServer()).forEach(player -> {
            ServerPlayNetworking.send(player, NetworkIdentifiers.UPDATE_OBFUSCATED_CACHE_S2C, buf);
        });
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        if (entity.getWorld().isClient() || entity.getServer() == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(entity.getUuid());
        buf.writeBoolean(false);
        PlayerLookup.all(entity.getServer()).forEach(player -> {
            ServerPlayNetworking.send(player, NetworkIdentifiers.UPDATE_OBFUSCATED_CACHE_S2C, buf);
        });
    }
}
