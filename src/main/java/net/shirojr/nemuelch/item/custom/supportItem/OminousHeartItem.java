package net.shirojr.nemuelch.item.custom.supportItem;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.ConfigInit;

public class OminousHeartItem extends Item {

    private int tickCount = 0;

    public OminousHeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;

        if (tickCount % 60 == 0) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(entity.getBlockPos());

            for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, entity.getPos(), ConfigInit.CONFIG.ominousHeartBeatRange)) {
                ServerPlayNetworking.send(player, NeMuelch.SOUND_PACKET_ID, buf);
            }
            tickCount = 0;
        }

        tickCount++;
    }
}
