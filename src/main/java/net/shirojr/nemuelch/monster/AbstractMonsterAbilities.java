package net.shirojr.nemuelch.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMonsterAbilities {
    protected final AbstractMonsterType monsterType;
    protected final LivingEntity self;

    private Boolean isNightBuffer;

    public AbstractMonsterAbilities(AbstractMonsterType monsterType) {
        this.monsterType = monsterType;
        this.self = this.monsterType.getProvider();
        this.isNightBuffer = null;
    }

    public LivingEntity getSelf() {
        return self;
    }

    @SuppressWarnings("unused")
    public AbstractMonsterType getMonsterType() {
        return monsterType;
    }

    public abstract void onAttackOther(PlayerEntity self, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult);

    public abstract void onKilledOther(LivingEntity attacker, LivingEntity victim);

    public abstract void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction);

    public abstract void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement);

    public abstract void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult);

    public abstract void onInteractEntity(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult);

    protected abstract void onNightfall();

    protected abstract void onDawn();

    public abstract void onStartSleeping(BlockPos blockPos);

    public abstract void onStopSleeping(BlockPos blockPos);

    public abstract void onKeybindPressed(ServerPlayerEntity player, int key);

    public void serverTick() {
        if (!(getSelf().getWorld() instanceof ServerWorld serverWorld)) return;
        if (isNightBuffer == null) {
            isNightBuffer = serverWorld.isNight();
        }
        if (serverWorld.isNight() != isNightBuffer) {
            if (serverWorld.isNight()) {
                onNightfall();
            } else {
                onDawn();
            }
            isNightBuffer = serverWorld.isNight();
        }
    }
}
