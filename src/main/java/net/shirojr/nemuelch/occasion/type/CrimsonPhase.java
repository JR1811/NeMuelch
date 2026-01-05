package net.shirojr.nemuelch.occasion.type;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionGrade;
import net.shirojr.nemuelch.occasion.util.OccasionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record CrimsonPhase(long defaultDuration) implements OccasionType {

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

    }

    @Override
    public void onActiveTick(World world, OccasionEntry entry) {

    }

    @Override
    public void onFinish(World world, OccasionEntry entry) {

    }

    @Override
    public void onPlayerJoinedWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {

    }

    @Override
    public void onPlayerLeftWorldWhileActive(ServerPlayerEntity player, OccasionEntry entry) {

    }
}
