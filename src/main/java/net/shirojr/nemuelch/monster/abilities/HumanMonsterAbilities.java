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

public class HumanMonsterAbilities extends AbstractMonsterAbilities {
    public HumanMonsterAbilities(AbstractMonsterType monsterType) {
        super(monsterType);
    }

    @Override
    public void onAttackOther(PlayerEntity self, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {

    }

    @Override
    public void onKilledOther(LivingEntity attacker, LivingEntity victim) {

    }

    @Override
    public void onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {

    }

    @Override
    public void onSteppedOn(ServerWorld serverWorld, LivingEntity self, MovementType movementType, Vec3d movement) {

    }

    @Override
    public void onInteractBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {

    }

    @Override
    public void onInteractEntity(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitResult) {

    }

    @Override
    protected void onNightfall() {

    }

    @Override
    protected void onDawn() {

    }

    @Override
    public void onStartSleeping(BlockPos blockPos) {

    }

    @Override
    public void onStopSleeping(BlockPos blockPos) {

    }

    @Override
    public void onKeybindPressed(ServerPlayerEntity player, int key) {

    }

    @Override
    public void serverTick() {
        super.serverTick();

    }
}
