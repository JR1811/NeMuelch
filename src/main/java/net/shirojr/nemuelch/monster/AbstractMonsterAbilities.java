package net.shirojr.nemuelch.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.GeneralMonsterComponent;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMonsterAbilities {
    protected final AbstractMonsterType monsterType;
    protected final LivingEntity self;

    private boolean isNightBuffer;

    public AbstractMonsterAbilities(AbstractMonsterType monsterType) {
        this.monsterType = monsterType;
        this.self = this.monsterType.getProvider();
        this.isNightBuffer = self.getWorld().isNight();
    }

    public LivingEntity getSelf() {
        return self;
    }

    @SuppressWarnings("unused")
    public AbstractMonsterType getMonsterType() {
        return monsterType;
    }

    protected boolean isNotDominant() {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(self);
        return !monsterComponent.getDominatingMonsterTypes().contains(this.monsterType);
    }

    public final void onAttackOther(PlayerEntity self, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {
        if (isNotDominant()) return;
        doOnAttackOther(self, world, hand, target, hitResult);
    }

    public final void onKilledOther(LivingEntity attacker, LivingEntity victim) {
        if (isNotDominant()) return;
        doOnKilledOther(attacker, victim);
    }

    public final void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (isNotDominant()) return;
        doOnAttackBlock(player, world, hand, pos, direction);
    }

    public final void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {
        if (isNotDominant()) return;
        doOnSteppedOn(serverWorld, self, movementType, movement);
    }

    public final void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (isNotDominant()) return;
        doOnInteractBlock(player, world, hand, hitResult);
    }

    public final ActionResult onInteractEntity(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {
        if (isNotDominant()) return ActionResult.PASS;
        return doOnInteractEntity(player, world, hand, target, hitResult);
    }

    protected final void onNightfall() {
        if (isNotDominant()) return;
        doOnNightfall();
    }

    protected final void onDawn() {
        if (isNotDominant()) return;
        doOnDawn();
    }

    public final void onStartSleeping(BlockPos blockPos) {
        if (isNotDominant()) return;
        doOnStartSleeping(blockPos);
    }

    public final void onStopSleeping(BlockPos blockPos) {
        if (isNotDominant()) return;
        doOnStopSleeping(blockPos);
    }

    public final void onKeybindPressed(ServerPlayerEntity player, int key) {
        if (isNotDominant()) return;
        doOnKeybindPressed(player, key);
    }

    public void serverTick() {
        if (!(getSelf().getWorld() instanceof ServerWorld serverWorld)) return;
        if (serverWorld.isNight() != isNightBuffer) {
            if (serverWorld.isNight()) {
                onNightfall();
            } else {
                onDawn();
            }
            isNightBuffer = serverWorld.isNight();
        }
        if (isNotDominant()) return;
        doOnServerTick();
    }

    protected abstract void doOnAttackOther(PlayerEntity self, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult);

    protected abstract void doOnKilledOther(LivingEntity attacker, LivingEntity victim);

    protected abstract void doOnAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction);

    protected abstract void doOnSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement);

    protected abstract void doOnInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult);

    protected abstract ActionResult doOnInteractEntity(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult);

    protected abstract void doOnNightfall();

    protected abstract void doOnDawn();

    protected abstract void doOnStartSleeping(BlockPos blockPos);

    protected abstract void doOnStopSleeping(BlockPos blockPos);

    protected abstract void doOnKeybindPressed(ServerPlayerEntity player, int key);

    protected abstract void doOnServerTick();
}
