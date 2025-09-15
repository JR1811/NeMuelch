package net.shirojr.nemuelch.compat.cca.implementation.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.compat.cca.component.monster.GeneralMonsterComponent;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import net.shirojr.nemuelch.monster.type.*;
import net.shirojr.nemuelch.monster.type.VampireMonsterType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GeneralMonsterComponentImpl implements GeneralMonsterComponent {
    public static final double DOMINANCE_SUM_MARGIN = 1e-6;

    private final LivingEntity provider;
    private final HashSet<AbstractMonsterType> monsterTypes;

    public GeneralMonsterComponentImpl(LivingEntity provider) {
        this.provider = provider;
        this.monsterTypes = new HashSet<>();
        this.monsterTypes.add(new VampireMonsterType(provider, 1, 1, 1));
        this.monsterTypes.add(new DryadMonsterType(provider));
        this.monsterTypes.add(new WerwolfMonsterType(provider));
        this.monsterTypes.add(new HumanMonsterType(provider));
    }

    @Nullable
    @Override
    public AbstractMonsterType getMonsterType(Identifier identifier) {
        for (AbstractMonsterType type : this.monsterTypes) {
            if (type.getIdentifier().equals(identifier)) return type;
        }
        return null;
    }

    @Override
    public Set<AbstractMonsterType> getActiveMonsterTypes() {
        HashSet<AbstractMonsterType> result = new HashSet<>();
        for (AbstractMonsterType type : this.monsterTypes) {
            if (type.getDominance() <= 0f) continue;
            result.add(type);
        }
        return result;
    }

    @Override
    public Set<AbstractMonsterType> getDominatingMonsterTypes() {
        float highestValue = -1f;
        HashSet<AbstractMonsterType> result = new HashSet<>();

        for (var entry : this.monsterTypes) {
            float dominance = entry.getDominance();
            if (dominance > highestValue) {
                highestValue = dominance;
                result.clear();
                result.add(entry);
            } else if (dominance == highestValue) {
                result.add(entry);
            }
        }
        return result;
    }

    // region Getter & Setter
    @Override
    public void setWithProportions(AbstractMonsterType type, float value) {
        Set<AbstractMonsterType> oldDominating = Collections.unmodifiableSet(this.getDominatingMonsterTypes());
        float valueClamped = MathHelper.clamp(value, 0, 1);
        float valueOld = type.getDominance();
        float sumOthersOld = 1 - valueOld;
        float remainingNew = 1 - valueClamped;

        for (var entry : this.monsterTypes) {
            if (entry.equals(type)) {
                entry.setDominance(valueClamped);
                continue;
            }
            float oldEntryValue = entry.getDominance();
            float redistributedEntryValue;
            if (sumOthersOld == 0) {
                redistributedEntryValue = remainingNew / (this.monsterTypes.size() - 1);
            } else {
                redistributedEntryValue = oldEntryValue * (remainingNew / sumOthersOld);
            }
            entry.setDominance(redistributedEntryValue);
        }
        Set<AbstractMonsterType> newDominating = Collections.unmodifiableSet(this.getDominatingMonsterTypes());
        handleDominantChanges(oldDominating, newDominating);
    }

    private static void handleDominantChanges(Set<AbstractMonsterType> oldDominating, Set<AbstractMonsterType> newDominating) {
        Set<AbstractMonsterType> addedDominating = new HashSet<>(newDominating);
        addedDominating.removeAll(oldDominating);
        Set<AbstractMonsterType> removedDominating = new HashSet<>(oldDominating);
        removedDominating.removeAll(newDominating);

        for (AbstractMonsterType addedEntry : addedDominating) {
            addedEntry.onMonsterTypeGainedDominance(addedEntry.getProvider());
        }

        for (AbstractMonsterType removedEntry : removedDominating) {
            removedEntry.onMonsterTypeLostDominance(removedEntry.getProvider());
        }
    }

    @Override
    public void reset() {
        for (var entry : this.monsterTypes) {
            entry.resetDominance();
        }
    }

    @Override
    public void renormalize() {
        float sum = 0f;
        for (AbstractMonsterType type : this.monsterTypes) {
            sum += type.getDominance();
        }
        if (Math.abs(sum - 1) < DOMINANCE_SUM_MARGIN) return;
        for (var entry : this.monsterTypes) {
            if (sum == 0) {
                entry.setDominance(1f / this.monsterTypes.size());
            } else {
                entry.setDominance(entry.getDominance() / sum);
            }
        }
    }
    // endregion


    @Override
    public void serverTick() {
        if (!(provider.getWorld() instanceof ServerWorld)) return;

        for (AbstractMonsterType entry : this.monsterTypes) {
            entry.serverTick();
        }
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if (nbt.contains("monsterTypes")) {
            NbtList nbtList = nbt.getList("monsterTypes", NbtElement.COMPOUND_TYPE);
            for (NbtElement nbtElement : nbtList) {
                NbtCompound nbtEntry = (NbtCompound) nbtElement;

                if (!nbtEntry.contains("identifier")) {
                    NeMuelch.LOGGER.warn("monster type [{}] nbt implementation not aligned with general retrieval", nbtEntry);
                    continue;
                }
                Identifier nbtEntryIdentifier = Identifier.tryParse(nbtEntry.getString("identifier"));
                if (nbtEntryIdentifier == null) continue;

                for (AbstractMonsterType registeredType : this.monsterTypes) {
                    if (!registeredType.getIdentifier().equals(nbtEntryIdentifier)) continue;
                    registeredType.applyNbt(nbtEntry);
                    break;
                }
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        NbtList nbtList = new NbtList();
        for (AbstractMonsterType type : this.monsterTypes) {
            nbtList.add(type.asNbt());
        }
        nbt.put("monsterTypes", nbtList);
    }

    @Override
    public void sync() {
        NeMuelchComponents.MONSTER.sync(this.provider);
    }
}
