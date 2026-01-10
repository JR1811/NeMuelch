package net.shirojr.nemuelch.occasion.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import org.joml.Vector4f;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public interface OccasionType {
    Text getName();

    default List<Text> getDescription() {
        return List.of(Text.literal("- No Description -"));
    }

    OccasionGrade getGrade();

    long defaultDuration();

    default Predicate<OccasionType> excludeOther() {
        return type -> false;
    }

    void onStart(World world, OccasionEntry entry);

    void onActiveTick(World world, OccasionEntry entry);

    void onFinish(World world, OccasionEntry entry);

    void onPlayerJoinedWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry);

    void onPlayerLeftWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry);

    default Optional<Vector4f> getSunColor(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    default Optional<Vector4f> getMoonColor(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    default Optional<Identifier> getSunSprite(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    default Optional<Identifier> getMoonSprite(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    /**
     * Default value: 30
     */
    default Optional<Float> getSunSize(World world, OccasionEntry entry) {
        return Optional.empty();
    }

    /**
     * Default value: 20
     */
    default Optional<Float> getMoonSize(World world, OccasionEntry entry) {
        return Optional.empty();
    }
}
