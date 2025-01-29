package net.shirojr.nemuelch.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record AppliedLivingEntityAttributeModifier(EntityAttribute attribute,
                                                   @Nullable EntityAttributeModifier modifier,
                                                   EntityAttributeModifier.Operation operation) {
    public UUID getId() {
        if (this.modifier() == null) {
            throw new NullPointerException("Applied Attribute Modifier instance didn't have its modifier set yet");
        }
        return this.modifier().getId();
    }

    @Nullable
    public static AppliedLivingEntityAttributeModifier fromNbt(NbtCompound activeAttributeNbt) {
        EntityAttribute nbtAttribute = Registry.ATTRIBUTE.get(new Identifier(activeAttributeNbt.getString("attribute")));
        EntityAttributeModifier nbtModifier = !activeAttributeNbt.contains("modifier") ? null : EntityAttributeModifier.fromNbt(activeAttributeNbt.getCompound("modifier"));
        EntityAttributeModifier.Operation nbtOperation = EntityAttributeModifier.Operation.fromId(activeAttributeNbt.getInt("operation"));
        if (nbtAttribute == null || nbtModifier == null || nbtOperation == null) return null;
        return new AppliedLivingEntityAttributeModifier(nbtAttribute, nbtModifier, nbtOperation);
    }

    public void toNbt(NbtCompound nbt) {
        NbtCompound activeAttributeNbt = new NbtCompound();

        Identifier attributeId = Registry.ATTRIBUTE.getId(this.attribute());
        if (attributeId == null) {
            throw new NullPointerException("Applied Attribute modifier's attribute was not available in the registry");
        }
        activeAttributeNbt.putString("attribute", attributeId.toString());

        if (this.modifier() == null) {
            throw new NullPointerException("Applied Attribute Modifier instance didn't have its modifier set when saving");
        }
        activeAttributeNbt.put("modifier", this.modifier().toNbt());

        activeAttributeNbt.putInt("operation", this.operation.getId());

        nbt.put(this.modifier().getId().toString(), activeAttributeNbt);
    }
}
