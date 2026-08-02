package net.shirojr.nemuelch.monster.abilities.custom;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.shirojr.nemuelch.monster.abilities.PassiveAbility;
import net.shirojr.nemuelch.util.constants.NbtKeys;

import java.util.UUID;

public class PassiveSpeedModifierAbility extends PassiveAbility {
    private static final String ATTRIBUTE_NAME = "Passive Speed Adjustment Ability";
    private static final UUID SPEED_ATTRIBUTE_UUID = UUID.fromString("ff64aafd-a59c-4d5b-87d5-bfd9fd94aaff");

    private double speedMultiplier;

    public PassiveSpeedModifierAbility(PlayerEntity provider, double speedMultiplier) {
        super(provider);
        this.speedMultiplier = speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public void refreshSpeedMultiplier() {
        if (this.provider == null) return;
        EntityAttributeInstance attributeInstance = this.provider.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attributeInstance == null) return;
        attributeInstance.removeModifier(SPEED_ATTRIBUTE_UUID);

        EntityAttributeModifier attributeModifier = new EntityAttributeModifier(
                SPEED_ATTRIBUTE_UUID, ATTRIBUTE_NAME, this.speedMultiplier, EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        );
        attributeInstance.addPersistentModifier(attributeModifier);
    }

    public void clearSpeedMultiplier() {
        EntityAttributeInstance attributeInstance = this.provider.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attributeInstance == null) return;
        attributeInstance.removeModifier(SPEED_ATTRIBUTE_UUID);
    }

    @Override
    public void onAdded() {
        this.refreshSpeedMultiplier();
    }

    @Override
    public void onRemoved() {
        this.clearSpeedMultiplier();
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
        if (nbt.contains(NbtKeys.PASSIVE_SPEED_MULTIPLIER)) {
            this.speedMultiplier = nbt.getDouble(NbtKeys.PASSIVE_SPEED_MULTIPLIER);
        }
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        nbt.putDouble(NbtKeys.PASSIVE_SPEED_MULTIPLIER, this.speedMultiplier);
    }
}
