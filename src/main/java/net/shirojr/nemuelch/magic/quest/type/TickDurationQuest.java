package net.shirojr.nemuelch.magic.quest.type;

import net.shirojr.nemuelch.magic.Quest;
import net.shirojr.nemuelch.magic.Reward;

public abstract class TickDurationQuest extends Quest {
    protected final int completionTicks;

    protected int currentTicks = 0;

    public TickDurationQuest(String name, Reward reward, int completionTicks) {
        super(name, reward);
        this.completionTicks = completionTicks;
    }

    public void tick() {
        this.currentTicks++;
        if (this.currentTicks >= completionTicks) {
            markCompleted();
        }
    }
}
