package net.shirojr.nemuelch.monster.type;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.monster.AbstractMonsterAbilities;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.abilities.DryadMonsterAbilities;

public class DryadMonsterType extends AbstractMonsterType {
    public static final Identifier IDENTIFIER = NeMuelch.getId("dryad");

    public DryadMonsterType(LivingEntity provider) {
        super(IDENTIFIER, provider, 0f);
    }

    @Override
    protected AbstractMonsterAbilities createAbilities() {
        return new DryadMonsterAbilities(this);
    }

    @Override
    public void onMonsterTypeGainedDominance(LivingEntity provider) {
        super.onMonsterTypeGainedDominance(provider);
        this.playSoundForProvider(NeMuelchSounds.PLANT_SWING, SoundCategory.PLAYERS, provider.getPos(), 1f, 1f);
    }

    @Override
    public void onMonsterTypeLostDominance(LivingEntity provider) {
        super.onMonsterTypeLostDominance(provider);
    }

    @Override
    public void serverTick() {

    }

    @Override
    protected void readCustomNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomNbt(NbtCompound nbt) {

    }
}
