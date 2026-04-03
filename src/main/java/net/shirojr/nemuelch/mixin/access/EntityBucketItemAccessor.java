package net.shirojr.nemuelch.mixin.access;

import net.minecraft.entity.EntityType;
import net.minecraft.item.EntityBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityBucketItem.class)
public interface EntityBucketItemAccessor {
    @Accessor("entityType")
    EntityType<?> nemuelch$getEntityType();
}
