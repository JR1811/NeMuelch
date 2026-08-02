package net.shirojr.nemuelch.monster.abilities.data;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.monster.abilities.custom.DrinkBloodAbility;
import net.shirojr.nemuelch.monster.abilities.util.MonsterTypeData;

public class VampireData implements MonsterTypeData, DrinkBloodAbility.BloodDrinker {
    private long consumedBlood;
    private Rank rank;

    public VampireData(Rank rank) {
        this.rank = rank;
    }

    @Override
    public long getConsumedBlood() {
        return this.consumedBlood;
    }

    @Override
    public void setConsumedBlood(long consumedBlood) {
        this.consumedBlood = Math.min(Math.max(consumedBlood, 0), this.getBloodIntakeCapacity());
    }

    @Override
    public void addConsumedBlood(long consumedBlood) {
        this.setConsumedBlood(this.getConsumedBlood() + consumedBlood);
    }

    @Override
    public long getBloodIntakeCapacity() {
        return this.getRank().getBloodIntakeCapacity();
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    @Override
    public void onDrankBlood(PlayerEntity user, LivingEntity target) {

    }

    @Override
    public void toNbt(NbtCompound nbt) {

    }

    @Override
    public void fromNbt(NbtCompound nbt) {

    }

    @SuppressWarnings("UnstableApiUsage")
    public enum Rank {
        SCUM(FluidConstants.DROPLET * 3, 15f, 1.0f),
        PEASANT(FluidConstants.DROPLET * 50, 17.5f, 0.9f),
        SERVANT(FluidConstants.DROPLET * 1000, 20f, 0.7f),
        KING(FluidConstants.BUCKET, 25f, 0.5f),
        EMPEROR(FluidConstants.BUCKET * 20, 35f, 0.3f),
        GOD(FluidConstants.BUCKET * 200, 60f, 0.2f);

        private final long bloodIntakeCapacity;
        private final float damageMultiplier;
        private final float antiVampireResistance;

        Rank(long bloodIntakeCapacity, float damageMultiplier, float antiVampireResistance) {
            this.bloodIntakeCapacity = bloodIntakeCapacity;
            this.damageMultiplier = damageMultiplier;
            this.antiVampireResistance = antiVampireResistance;
        }

        public long getBloodIntakeCapacity() {
            return bloodIntakeCapacity;
        }

        public float getDamageMultiplier() {
            return damageMultiplier;
        }

        public float getAntiVampireResistance() {
            return antiVampireResistance;
        }

        public static Rank get(float normalizedBlood) {
            Rank highestMatch = SCUM;
            while (highestMatch.getNext().bloodIntakeCapacity <= normalizedBlood) {
                highestMatch = highestMatch.getNext();
                if (highestMatch.equals(GOD)) {
                    break;
                }
            }
            return highestMatch;
        }

        public Rank getBefore() {
            int indexBefore = Math.max(this.ordinal() - 1, 0);
            return Rank.values()[indexBefore];
        }

        public Rank getNext() {
            int nextIndex = Math.min(this.ordinal() + 1, Rank.values().length - 1);
            return Rank.values()[nextIndex];
        }
    }
}
