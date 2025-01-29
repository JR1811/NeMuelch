package net.shirojr.nemuelch.magic.quest.type;

import net.shirojr.nemuelch.magic.Quest;
import net.shirojr.nemuelch.magic.Reward;

public abstract class CountingQuest extends Quest {
    protected final int completionAmount;

    protected int currentAmount = 0;

    public CountingQuest(String name, Reward reward, int completionAmount) {
        super(name, reward);
        this.completionAmount = completionAmount;
    }

    public void increment(int count) {
        this.currentAmount = Math.min(this.currentAmount + count, this.completionAmount);
        if (this.currentAmount >= this.completionAmount) {
            markCompleted();
        }
    }

    public void decrement(int count) {
        this.currentAmount = Math.max(this.currentAmount - count, 0);
        if (this.currentAmount >= this.completionAmount) {
            markCompleted();
        }
    }

    public void increment() {
        this.increment(1);
    }

    public void decrement() {
        this.decrement(1);
    }
}
