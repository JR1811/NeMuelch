package net.shirojr.nemuelch.compat.cca.util.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.NeMuelch;

public class VampireMonsterType extends AbstractMonsterType {
    public VampireMonsterType(LivingEntity provider) {
        super(NeMuelch.getId("vampire"), provider, 0f);
    }

    @Override
    public void onMonsterTypeGainedDominance(LivingEntity provider) {
        super.onMonsterTypeGainedDominance(provider);
        this.playSoundForProvider(SoundEvents.ENTITY_BAT_LOOP, SoundCategory.PLAYERS, provider.getPos(), 1f, 0.8f);
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
