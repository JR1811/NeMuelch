package net.shirojr.nemuelch.screen.handler;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.shirojr.nemuelch.block.entity.ParticleEmitterBlockEntity;
import net.shirojr.nemuelch.network.NeMuelchC2SPacketHandler;
import net.shirojr.nemuelch.util.helper.ParticleDataNetworkingHelper;

public class ParticleEmitterBlockScreenHandler extends ScreenHandler {
    private ScreenHandlerContext context;
    private ParticleEmitterBlockEntity.ParticleData storedParticle;
    private PlayerEntity player;

    public ParticleEmitterBlockScreenHandler(int syncId, PlayerEntity player) {
        this(syncId, ScreenHandlerContext.EMPTY,
                ParticleEmitterBlockEntity.ParticleData.getDefaultData(), player);
        this.player = player;
    }

    public ParticleEmitterBlockScreenHandler(int syncId, ScreenHandlerContext context,
                                             ParticleEmitterBlockEntity.ParticleData storedParticle, PlayerEntity player) {
        super(NeMuelchScreenHandlers.PARTICLE_EMITTER_SCREEN_HANDLER, syncId);
        this.context = context;
        this.storedParticle = storedParticle;
        if (player != null) this.player = player;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.isCreative() || player.isSpectator();
    }

    public ParticleEmitterBlockEntity.ParticleData getParticleData() {
        this.context.run((world, pos) -> {
            if (!(world.getBlockEntity(pos) instanceof ParticleEmitterBlockEntity blockEntity)) return;
            if (blockEntity.getCurrentParticle() == null) return;
            this.storedParticle = blockEntity.getCurrentParticle();
        });

        return this.storedParticle;
    }

    public void sendParticleDataUpdatePacket(ParticleEmitterBlockEntity.ParticleData data) {
        if (!(this.player instanceof ClientPlayerEntity)) return;
        PacketByteBuf buf = PacketByteBufs.create();
        ParticleDataNetworkingHelper.addToBuf(buf, data);
        ClientPlayNetworking.send(NeMuelchC2SPacketHandler.PARTICLE_EMITTER_UPDATE_CHANNEL, buf);
    }

    public ScreenHandlerContext getScreenHandlerContext() {
        return this.context;
    }
}
