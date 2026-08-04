package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.monster.abilities.util.AbilityRegistrar;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

public class AbilityContainer implements AbilityRegistrar {
    private final HashMap<Class<? extends Ability>, Ability> abilities;

    public AbilityContainer() {
        this.abilities = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> Optional<T> get(Class<T> type) {
        return Optional.ofNullable((T) this.abilities.get(type));
    }

    @Override
    public <T extends Ability> AbilityRegistrar add(T entry) {
        this.abilities.put(entry.getClass(), entry);
        return this;
    }

    public <T extends Ability> void modify(Class<T> type, Consumer<T> modifier) {
        get(type).ifPresent(modifier);
    }

    @Override
    public void clear() {
        this.abilities.clear();
    }


    public void pressedKey(int index, boolean pressed, ServerPlayerEntity player) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof ActiveAbility activeAbility)) continue;
            activeAbility.keybindInteraction(index, player, pressed);
        }
    }

    public void onPickedUpItem(PlayerEntity player, ItemEntity itemEntity) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onPickedUpItem(player, itemEntity);
        }
    }

    public void onAttackOther(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onAttackEntity(player, world, hand, other, hitResult);
        }
    }

    public void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onAttackBlock(player, world, hand, pos, direction);
        }
    }

    public void onStartSleeping(BlockPos blockPos) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onStartSleeping(blockPos);
        }
    }

    public void onStopSleeping(BlockPos blockPos) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onStopSleeping(blockPos);
        }
    }

    public void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onInteractBlock(player, world, hand, hitResult);
        }
    }

    public void onInteractEntity(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onInteractEntity(player, world, hand, other, hitResult);
        }
    }

    public void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onSteppedOn(serverWorld, self, movementType, movement);
        }
    }

    public void onKilledOther(LivingEntity attacker, LivingEntity victim) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) continue;
            passiveAbility.onKilledOther(attacker, victim);
        }
    }

    public void serverTick(ServerPlayerEntity player) {
        this.abilities.values().forEach(ability -> ability.tickServer(player));
    }

    public void readFromNbt(@NotNull NbtCompound tag) {
        NbtCompound abilityDataNbt = tag.getCompound(NeMuelchNbtKeys.MONSTER_DATA);
        for (Ability ability : this.abilities.values()) {
            String key = ability.getClass().getName();
            if (abilityDataNbt.contains(key)) {
                ability.fromNbt(abilityDataNbt.getCompound(key));
            }
        }
    }

    public void writeToNbt(NbtCompound tag) {
        NbtCompound abilityDataNbt = new NbtCompound();
        for (Ability ability : this.abilities.values()) {
            NbtCompound abilityNbt = new NbtCompound();
            ability.toNbt(abilityNbt);
            abilityDataNbt.put(ability.getClass().getName(), abilityNbt);
        }
        tag.put(NeMuelchNbtKeys.MONSTER_DATA, abilityDataNbt);
    }
}
