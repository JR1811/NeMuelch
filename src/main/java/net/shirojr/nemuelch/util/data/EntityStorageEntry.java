package net.shirojr.nemuelch.util.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.mixin.access.EntityBucketItemAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record EntityStorageEntry(@NotNull EntityType<?> type, @NotNull NbtCompound data) {
    public static EntityStorageEntry create(@NotNull Entity entity) {
        return new EntityStorageEntry(entity.getType(), entity.writeNbt(new NbtCompound()));
    }

    @Nullable
    public static EntityStorageEntry createFromBucketStack(ItemStack stack, World world) {
        if (!(stack.getItem() instanceof EntityBucketItem bucketItem)) return null;
        EntityType<?> entityType = ((EntityBucketItemAccessor) bucketItem).nemuelch$getEntityType();

        Entity tempEntity = entityType.create(world);
        if (tempEntity == null) return null;

        NbtCompound fullNbt = tempEntity.writeNbt(new NbtCompound());
        tempEntity.discard();

        NbtCompound bucketNbt = stack.getNbt();
        if (bucketNbt != null) {
            fullNbt.copyFrom(bucketNbt);
        }

        return new EntityStorageEntry(entityType, fullNbt);
    }

    @Nullable
    public Entity getEntity(World world) {
        Entity entity = this.type.create(world);
        if (entity == null) return null;
        entity.readNbt(this.data);
        entity.setUuid(UUID.randomUUID());
        return entity;
    }

    @Nullable
    public Entity spawn(ServerWorld world, Vec3d pos) {
        Entity entity = getEntity(world);
        if (entity == null) return null;
        entity.setPosition(pos);
        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, entity.getYaw(), entity.getPitch());
        world.spawnEntity(entity);
        return entity;
    }

    public void toNbt(NbtCompound nbt) {
        Identifier identifier = EntityType.getId(this.type);
        if (identifier == null) {
            throw new IllegalStateException("Tried to create EntityType Storage Entry of [%s] which is not present".formatted(this.type));
        }
        nbt.putString("EntityType", identifier.toString());
        nbt.put("EntityData", this.data);
    }

    @Nullable
    public static EntityStorageEntry fromNbt(NbtCompound nbt) {
        Optional<EntityType<?>> entityType = EntityType.get(nbt.getString("EntityType"));
        if (entityType.isEmpty()) return null;
        NbtCompound entityData = nbt.getCompound("EntityData");
        return new EntityStorageEntry(entityType.get(), entityData);
    }
}
