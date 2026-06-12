package net.shirojr.nemuelch.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.shirojr.nemuelch.compat.cca.implementation.MiscEntityComponent;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

public class EntitySlowingFeature {
    public static final UUID ATTRIBUTE_UUID = UUID.fromString("23a1974a-43d6-4545-a20a-93e1c02c110b");
    public static final String HINT_TRANSLATION_KEY = "info.nemuelch.slowing";
    public static final int MIN_SLOWING = 0, MAX_SLOWING = -1;

    public static boolean hasSpeedEntityAttribute(Entity entity) {
        return entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    public static void handleScroll(LivingEntity entity, double delta) {
        EntityAttributeInstance speedAttribute = getTemporarySpeedAttributeInstance(entity);
        if (speedAttribute == null) return;
        setTemporarySpeed(entity, speedAttribute, current -> MathHelper.clamp(current + (delta * NeMuelchConfigInit.CONFIG.speedLimiterIncrement), MAX_SLOWING, MIN_SLOWING), false);
    }

    @Nullable
    public static EntityAttributeInstance getTemporarySpeedAttributeInstance(LivingEntity entity) {
        return entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    public static void setTemporarySpeed(LivingEntity entity, EntityAttributeInstance speedAttribute, DoubleUnaryOperator oldToNewHandler, boolean force) {
        if (!force && MiscEntityComponent.get(entity).isSlowingLocked()) return;
        EntityAttributeModifier modifier = speedAttribute.getModifier(ATTRIBUTE_UUID);
        double current = modifier != null ? modifier.getValue() : MIN_SLOWING;
        double newValue = oldToNewHandler.applyAsDouble(current);
        speedAttribute.removeModifier(ATTRIBUTE_UUID);
        if (newValue != MIN_SLOWING) {
            speedAttribute.addTemporaryModifier(
                    new EntityAttributeModifier(
                            ATTRIBUTE_UUID,
                            "Controlled Temporary Speed Limiter",
                            newValue,
                            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                    )
            );
        }
        if (entity instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.translatable(HINT_TRANSLATION_KEY, asPercentage(newValue)), true);
        }
    }

    public static int asPercentage(double value) {
        return MathHelper.clamp((int) (Math.round((value + 1) * 100)), 0, 100);
    }
}
