package net.shirojr.nemuelch.magic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public abstract class Quest {
    protected final String descriptionKey;
    protected final Reward reward;

    private boolean completed;

    public Quest(String name, Reward reward) {
        this.descriptionKey = "quest.nemuelch." + name;
        this.reward = reward;
        this.completed = false;
    }

    public abstract Identifier getIdentifier();

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void checkProgress(LivingEntity entity) {
        if (isCompleted()) {
            this.reward.apply(entity);
        }
    }
}
