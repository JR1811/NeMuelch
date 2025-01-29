package net.shirojr.nemuelch.magic.reward;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.shirojr.nemuelch.magic.Reward;
import net.shirojr.nemuelch.util.AppliedLivingEntityAttributeModifier;
import net.shirojr.nemuelch.util.wrapper.Buffable;

import java.util.UUID;

public class TemporaryStatReward implements Reward {
    private final String name;
    private final EntityAttribute attribute;
    private final EntityAttributeModifier.Operation operation;
    private final double value;
    private final int durationTicks;

    public TemporaryStatReward(String name,
                               EntityAttribute attribute, EntityAttributeModifier.Operation operation,
                               double value, int durationTicks) {
        this.name = name;
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
        this.durationTicks = durationTicks;
    }

    @Override
    public void apply(LivingEntity entity) {
        if (!(entity instanceof Buffable entityBuff)) return;
        EntityAttributeModifier modifier = new EntityAttributeModifier(UUID.randomUUID(), name, value, operation);
        AppliedLivingEntityAttributeModifier appliedModifier = new AppliedLivingEntityAttributeModifier(attribute, modifier, operation);
        entityBuff.nemuelch$applyNewAttributeModifier(appliedModifier, this.durationTicks);
    }
}
