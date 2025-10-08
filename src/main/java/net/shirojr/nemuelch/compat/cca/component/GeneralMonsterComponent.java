package net.shirojr.nemuelch.compat.cca.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.compat.cca.NeMuelchComponents;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@SuppressWarnings("unused")
public interface GeneralMonsterComponent extends Component, ServerTickingComponent, AutoSyncedComponent {
    Identifier KEY = NeMuelch.getId("monster");

    static GeneralMonsterComponent get(LivingEntity entity) {
        return NeMuelchComponents.MONSTER.get(entity);
    }

    @Nullable
    AbstractMonsterType getMonsterType(Identifier identifier);

    /**
     * @return all {@link AbstractMonsterType AbstractMonsterTypes} which have a {@link AbstractMonsterType#getDominance() dominance} above 0
     */
    Set<AbstractMonsterType> getActiveMonsterTypes();

    /**
     * @return dominating {@link AbstractMonsterType AbstractMonsterTypes}. There are multiple if top contender share same values
     */
    Set<AbstractMonsterType> getDominatingMonsterTypes();

    /**
     * Sets the value of the specified {@link AbstractMonsterType} (including normalization clamp) and adjust proportions of others accordingly
     */
    void setWithProportions(AbstractMonsterType type, float value);

    default void addWithProportions(AbstractMonsterType type, float delta) {
        float clampedValue = MathHelper.clamp(type.getDominance() + delta, 0f, 1f);
        this.setWithProportions(type, clampedValue);
    }

    /**
     * Resets values to the {@link AbstractMonsterType} default values
     */
    void reset();

    /**
     * Aligns values proportionally back to simplex design (sum of 1)
     */
    void renormalize();

    void sync();
}
