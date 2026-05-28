package net.shirojr.nemuelch.occasion.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class OccasionType implements EntityStrengthener, EntityXPModifier {
    public static final float ORIGINAL_MOON_SIZE = 20;
    public static final float ORIGINAL_SUN_SIZE = 30;

    public abstract Text getName();

    public List<Text> getDescription() {
        return List.of(Text.literal("- No Description -"));
    }

    public abstract OccasionGrade getGrade();

    public abstract long defaultDuration();

    public Predicate<OccasionType> excludeOther() {
        return type -> false;
    }

    public abstract void onStart(World world, OccasionEntry entry);

    public abstract void onActiveTick(World world, OccasionEntry entry);

    public abstract void onFinish(World world, OccasionEntry entry);

    public abstract void onPlayerJoinedWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry);

    public abstract void onPlayerLeftWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry);

    public Optional<Vector4f> getSunColor(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    public Optional<Vector4f> getMoonColor(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    public Optional<Identifier> getSunSprite(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    public Optional<Identifier> getMoonSprite(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    /**
     * Default value: 30
     */
    public Optional<Float> getSunSize(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    /**
     * Default value: 20
     */
    public OptionalDouble getMoonSize(World world, OccasionEntry entry) {
        return OptionalDouble.empty();
    }

    public OptionalInt getGlobalWaterColor(BlockRenderView world, BlockPos pos) {
        return OptionalInt.empty();
    }

    public OptionalInt getFogWaterColor(ClientWorld world) {
        return OptionalInt.empty();
    }

    public OptionalDouble getEntitySoundPitch(double original) {
        return OptionalDouble.empty();
    }

    public OptionalDouble getEntitySoundVolume(double original) {
        return OptionalDouble.empty();
    }
}
