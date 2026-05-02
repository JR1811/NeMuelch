package net.shirojr.nemuelch.misc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;

public class EntitySlowingFeature {
    public static final UUID ATTRIBUTE_UUID = UUID.fromString("23a1974a-43d6-4545-a20a-93e1c02c110b");
    public static final String HINT_TRANSLATION_KEY = "info.nemuelch.slowing";

    public static void handleScroll(LivingEntity entity, double delta) {
        EntityAttributeInstance speedAttribute = entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute == null) return;
        EntityAttributeModifier modifier = speedAttribute.getModifier(ATTRIBUTE_UUID);
        double current = modifier != null ? modifier.getValue() : 0;
        speedAttribute.removeModifier(ATTRIBUTE_UUID);
        double newValue = MathHelper.clamp(current + (delta * 0.1), -1, 0);
        speedAttribute.addTemporaryModifier(
                new EntityAttributeModifier(
                        ATTRIBUTE_UUID, "Controlled Speed", newValue, EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                )
        );
        if (entity instanceof ServerPlayerEntity player) {
            int percentage = MathHelper.clamp((int) ((newValue + 1) * 100), 0, 100);
            player.sendMessage(Text.translatable(HINT_TRANSLATION_KEY, percentage), true);
        }
    }
}
