package net.shirojr.nemuelch.compat.cca.util.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchSounds;

public class HumanMonsterType extends AbstractMonsterType {
    public HumanMonsterType(LivingEntity provider) {
        super(NeMuelch.getId("human"), provider, 1f);
    }

    @Override
    public void onMonsterTypeGainedDominance(LivingEntity provider) {
        super.onMonsterTypeGainedDominance(provider);
        this.playSoundForProvider(NeMuelchSounds.HUMAN_GROWL, SoundCategory.PLAYERS, provider.getPos(), 1f, 1f);
    }

    @Override
    public void onMonsterTypeLostDominance(LivingEntity provider) {
        super.onMonsterTypeLostDominance(provider);
    }

    @Override
    public void serverTick() {

    }

    @Override
    protected void writeCustomNbt(NbtCompound nbt) {

    }

    @Override
    protected void readCustomNbt(NbtCompound nbt) {

    }
}
