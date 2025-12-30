package net.shirojr.nemuelch.occasion.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.nemuelch.occasion.OccasionEntry;

import java.util.List;
import java.util.function.Predicate;

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

    void onPlayerLeftWorldWhileActive(ServerWorld world, OccasionEntry entry);
}
