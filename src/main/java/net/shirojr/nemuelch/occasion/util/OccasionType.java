package net.shirojr.nemuelch.occasion.util;

import net.minecraft.server.network.ServerPlayerEntity;
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

    @SuppressWarnings("unused")
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
}
