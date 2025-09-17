package net.shirojr.nemuelch.monster.abilities;

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
import net.shirojr.nemuelch.monster.AbstractMonsterAbilities;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

public class VampireMonsterAbilities extends AbstractMonsterAbilities {
    public VampireMonsterAbilities(AbstractMonsterType monsterType) {
        super(monsterType);
    }

    @Override
    protected void doOnAttackOther(PlayerEntity self, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {

    }

    @Override
    protected void doOnKilledOther(LivingEntity attacker, LivingEntity victim) {

    }

    @Override
    protected void doOnAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {

    }

    @Override
    protected void doOnSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {

    }

    @Override
    protected void doOnInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

    }

    @Override
    protected void doOnInteractEntity(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {

    }

    @Override
    protected void doOnNightfall() {

    }

    @Override
    protected void doOnDawn() {

    }

    @Override
    protected void doOnStartSleeping(BlockPos blockPos) {

    }

    @Override
    protected void doOnStopSleeping(BlockPos blockPos) {

    }

    @Override
    protected void doOnKeybindPressed(ServerPlayerEntity player, int key) {

    }

    @Override
    public void serverTick() {
        super.serverTick();

    }
}
