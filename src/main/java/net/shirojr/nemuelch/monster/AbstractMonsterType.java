package net.shirojr.nemuelch.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.monster.abilities.Ability;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;
import net.shirojr.nemuelch.monster.abilities.PassiveAbility;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class AbstractMonsterType implements MonsterTransitionCallback {
    private final HashMap<Class<? extends Ability>, Ability> abilities;

    public AbstractMonsterType() {
        this.abilities = new HashMap<>();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbility(Class<T> type) {
        return (T) this.abilities.get(type);
    }

    public <T extends Ability> void modifyAbility(Class<T> type, Consumer<T> modifier) {
        T ability = getAbility(type);
        if (ability != null) {
            modifier.accept(ability);
        }
    }

    public void printExtraCommandInfo(ServerCommandSource source) {
    }

    public void pressedKey(int index, boolean pressed, ServerPlayerEntity player) {
        for (Ability ability : this.abilities.values()) {
            if (ability instanceof ActiveAbility activeAbility) {
                activeAbility.keybindInteraction(index, player, pressed);
            }
        }
    }

    public void onPickedUpItem(ServerPlayerEntity player, ItemEntity itemEntity) {
        for (Ability ability : this.abilities.values()) {
            if (ability instanceof PassiveAbility passiveAbility) {
                passiveAbility.onPickedUpItem(player, itemEntity);
            }
        }
    }

    public void onAttackOther(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onAttackEntity(player, world, hand, other, hitResult);
        }
    }

    public void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onAttackBlock(player, world, hand, pos, direction);
        }
    }

    public void onStartSleeping(BlockPos blockPos) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onStartSleeping(blockPos);
        }
    }

    public void onStopSleeping(BlockPos blockPos) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onStopSleeping(blockPos);
        }
    }

    public void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onInteractBlock(player,world, hand, hitResult);
        }
    }

    public void onInteractEntity(PlayerEntity player, World world, Hand hand, Entity other, @Nullable EntityHitResult hitResult) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onInteractEntity(player, world, hand, other, hitResult);
        }
    }

    public void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onSteppedOn(serverWorld, self, movementType, movement);
        }
    }

    public void onKilledOther(LivingEntity attacker, LivingEntity victim) {
        for (Ability ability : this.abilities.values()) {
            if (!(ability instanceof PassiveAbility passiveAbility)) return;
            passiveAbility.onKilledOther(attacker, victim);
        }
    }

    public void serverTick(ServerPlayerEntity player) {
        this.abilities.values().forEach(ability -> ability.tickServer(player));
    }

    @SuppressWarnings("SameParameterValue")
    protected void playSoundForProvider(LivingEntity entity, SoundEvent sound, SoundCategory category, Vec3d pos, float volume, float pitch) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        serverWorld.playSound(null, pos.x, pos.y, pos.z, sound, category, volume, pitch);
    }

    public final NbtCompound asNbt() {
        NbtCompound nbt = new NbtCompound();
        writeCustomNbt(nbt);
        return nbt;
    }

    public final void applyDataFromNbt(NbtCompound nbt) {
        readCustomNbt(nbt);
    }

    abstract protected void writeCustomNbt(NbtCompound nbt);

    abstract protected void readCustomNbt(NbtCompound nbt);
}
