package net.shirojr.nemuelch.magic.quest;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.magic.Reward;
import net.shirojr.nemuelch.magic.quest.type.TickDurationQuest;

public class NearbyEntityDurationQuest extends TickDurationQuest {
    private final LivingEntity entity, targetEntity;
    private final double distance;

    public NearbyEntityDurationQuest(String name, Reward reward, int completionTicks, LivingEntity entity, LivingEntity targetEntity, double distance) {
        super(name, reward, completionTicks);
        this.entity = entity;
        this.targetEntity = targetEntity;
        this.distance = distance;
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(NeMuelch.MOD_ID, "nearby_entity_duration");
    }

    @Override
    public void tick() {
        if (entity.getPos().squaredDistanceTo(targetEntity.getPos()) > distance * distance) {
            return;
        }
        super.tick();
    }

    @Override
    public void checkProgress(LivingEntity entity) {
        tick();
        super.checkProgress(entity);
    }
}
