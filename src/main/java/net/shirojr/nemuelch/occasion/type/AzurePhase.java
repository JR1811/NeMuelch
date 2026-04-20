package net.shirojr.nemuelch.occasion.type;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionGrade;
import net.shirojr.nemuelch.occasion.util.OccasionType;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AzurePhase extends OccasionType {
    private final long defaultDuration;
    private final int defaultTransitionDuration;

    public AzurePhase(long defaultDuration, int defaultTransitionDuration) {
        this.defaultDuration = defaultDuration;
        this.defaultTransitionDuration = defaultTransitionDuration;
    }

    @Override
    public Text getName() {
        return Text.translatable("occasion.nemuelch.azure_phase");
    }

    @Override
    public List<Text> getDescription() {
        List<Text> result = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            result.add(Text.translatable("occasion.nemuelch.azure_phase.desc" + i));
        }
        return result;
    }

    @Override
    public OccasionGrade getGrade() {
        return OccasionGrade.BENEFICIAL;
    }

    @Override
    public long defaultDuration() {
        return defaultDuration;
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

    @Override
    public Optional<Vector4f> getSunColor(World world, OccasionEntry entry) {
        return Optional.of(new Vector4f(0.1f, 0.2f, 0.8f, 1f));
    }

    @Override
    public Optional<Vector4f> getMoonColor(World world, OccasionEntry entry) {
        return Optional.of(new Vector4f(0.1f, 0.2f, 0.8f, 1f));
    }

    @Override
    public Optional<Float> getSunSize(World world, OccasionEntry entry) {
        return Optional.of(40f);
    }

    @Override
    public Optional<Float> getMoonSize(World world, OccasionEntry entry) {
        return Optional.of(30f);
    }

    public int defaultTransitionDuration() {
        return defaultTransitionDuration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AzurePhase) obj;
        return this.defaultDuration == that.defaultDuration &&
                this.defaultTransitionDuration == that.defaultTransitionDuration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultDuration, defaultTransitionDuration);
    }

    @Override
    public String toString() {
        return "AzurePhase[" +
                "defaultDuration=" + defaultDuration + ", " +
                "defaultTransitionDuration=" + defaultTransitionDuration + ']';
    }

}
