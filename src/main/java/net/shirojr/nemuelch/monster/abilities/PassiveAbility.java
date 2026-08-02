package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.monster.abilities.custom.PassiveSpeedModifierAbility;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class PassiveAbility implements Ability {
    protected final PlayerEntity provider;

    public PassiveAbility(PlayerEntity provider) {
        this.provider = provider;
    }

    public void onAdded() {
    }

    public void onRemoved() {
    }

    public void onPickedUpItem(PlayerEntity player, ItemEntity itemEntity) {
    }

    public void onAttackEntity(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
    }

    public void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
    }

    public void onStartSleeping(BlockPos blockPos) {
    }

    public void onStopSleeping(BlockPos blockPos) {
    }

    public void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
    }

    public void onInteractEntity(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
    }

    public void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {
    }

    public void onKilledOther(LivingEntity attacker, LivingEntity victim) {
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
    }

    @Override
    public void toNbt(NbtCompound nbt) {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PassiveSpeedModifierAbility other)) return false;
        return Objects.equals(this.provider, other.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.provider);
    }
}
