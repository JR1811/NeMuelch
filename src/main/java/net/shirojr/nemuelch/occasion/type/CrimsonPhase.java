package net.shirojr.nemuelch.occasion.type;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.satin.NeMuelchShaderManager;
import net.shirojr.nemuelch.network.util.NetworkIdentifiers;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionGrade;
import net.shirojr.nemuelch.occasion.util.OccasionType;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record CrimsonPhase(long defaultDuration, int defaultTransitionDuration) implements OccasionType {

    @Override
    public Text getName() {
        return Text.translatable("occasion.nemuelch.crimson_phase");
    }

    @Override
    public List<Text> getDescription() {
        List<Text> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(Text.translatable("occasion.nemuelch.crimson_phase.desc" + i));
        }
        return result;
    }

    @Override
    public Predicate<OccasionType> excludeOther() {
        return OccasionType.super.excludeOther();
    }

    @Override
    public OccasionGrade getGrade() {
        return OccasionGrade.DANGEROUS;
    }

    @Override
    public void onStart(World world, OccasionEntry entry) {
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
                buf.writeFloat(1f);
                buf.writeVarInt(defaultTransitionDuration());
                ServerPlayNetworking.send(target, NetworkIdentifiers.SHADER_TRANSITION_START, buf);
            }
        }
    }

    @Override
    public void onActiveTick(World world, OccasionEntry entry) {

    }

    @Override
    public void onFinish(World world, OccasionEntry entry) {
        if (world instanceof ServerWorld serverWorld) {
            MinecraftServer server = serverWorld.getServer();
            for (ServerPlayerEntity target : PlayerLookup.all(server)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
                buf.writeFloat(0f);
                buf.writeVarInt(defaultTransitionDuration());
                ServerPlayNetworking.send(target, NetworkIdentifiers.SHADER_TRANSITION_START, buf);
            }
        }
    }

    @Override
    public void onPlayerJoinedWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
        buf.writeFloat(1f);
        ServerPlayNetworking.send(player, NetworkIdentifiers.SHADER_INTENSITY_SETTER, buf);
    }

    @Override
    public void onPlayerLeftWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(NeMuelchShaderManager.getOrdinal(NeMuelchShaderManager.CRIMSON_PHASE));
        ServerPlayNetworking.send(player, NetworkIdentifiers.SHADER_CLEAR, buf);
    }

    @Override
    public Optional<Vector4f> getSunColor() {
        return Optional.empty();
    }

    @Override
    public Optional<Vector4f> getMoonColor() {
        return Optional.empty();
    }

    @Override
    public Optional<Identifier> getSunSprite() {
        return Optional.empty();
    }

    @Override
    public Optional<Identifier> getMoonSprite() {
        return Optional.of(NeMuelch.getId("textures/environment/moon_crimson_phase.png"));
    }

    @Override
    public Optional<Float> getSunSize() {
        return Optional.empty();
    }

    @Override
    public Optional<Float> getMoonSize() {
        return Optional.empty();
    }
}
