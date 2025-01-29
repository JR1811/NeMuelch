package net.shirojr.nemuelch.magic.quest;

import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.magic.Reward;
import net.shirojr.nemuelch.magic.quest.type.CountingQuest;

public class KillQuest extends CountingQuest {
    private final EntityType<?> entityType;

    public KillQuest(String name, Reward reward, int completionAmount, EntityType<?> type) {
        super(name, reward, completionAmount);
        this.entityType = type;
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(NeMuelch.MOD_ID, "kill_quest");
    }

    public boolean canAdd(EntityType<?> incomingKillType) {
        Identifier incomingType = Registry.ENTITY_TYPE.getId(incomingKillType);
        Identifier questType = Registry.ENTITY_TYPE.getId(this.entityType);
        return incomingType.equals(questType);
    }
}
