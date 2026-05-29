package net.shirojr.nemuelch.occasion.type;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.occasion.util.OccasionGrade;
import net.shirojr.nemuelch.occasion.util.OccasionType;

import java.util.Optional;

public class MaroonPhase extends OccasionType {
    private final long defaultDuration;

    public MaroonPhase(long defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    @Override
    public Text getName() {
        return Text.translatable("occasion.nemuelch.maroon_phase");
    }

    @Override
    public OccasionGrade getGrade() {
        return OccasionGrade.DANGEROUS;
    }

    @Override
    public long defaultDuration() {
        return defaultDuration;
    }

    @Override
    public Optional<Identifier> getSunSprite(World world, OccasionEntry entry) {
        return Optional.of(NeMuelch.getId("textures/environment/sun_maroon_phase.png"));
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
