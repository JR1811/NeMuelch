package net.shirojr.nemuelch.magic.quest.type;

import net.minecraft.server.world.ServerWorld;
import net.shirojr.nemuelch.magic.Quest;
import net.shirojr.nemuelch.magic.Reward;

public abstract class TimeQuest extends Quest {
    private long completionTime;

    public TimeQuest(String name, Reward reward, long activationWorldTime) {
        super(name, reward);
        this.completionTime = activationWorldTime;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public void checkTime(ServerWorld world) {
        if (world.getTime() == this.completionTime) {
            markCompleted();
        }
    }
}
