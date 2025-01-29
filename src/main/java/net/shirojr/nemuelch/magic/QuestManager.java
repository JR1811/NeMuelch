package net.shirojr.nemuelch.magic;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class QuestManager {
    private final HashMap<Identifier, Quest> activeQuests = new HashMap<>();

    public boolean addQuest(Quest quest) {
        if (this.activeQuests.containsKey(quest.getIdentifier())) return false;
        this.activeQuests.put(quest.getIdentifier(), quest);
        return true;
    }

    @Nullable
    public Quest removeQuest(Identifier identifier) {
        if (!this.activeQuests.containsKey(identifier)) return null;
        return this.activeQuests.remove(identifier);
    }

    public void checkActiveQuests(LivingEntity livingEntity) {
        for (Quest quest : this.activeQuests.values()) {
            quest.checkProgress(livingEntity);
        }
    }
}
