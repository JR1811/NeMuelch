package net.shirojr.nemuelch.monster.abilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.monster.AbstractMonsterAbilities;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

public class WerwolfMonsterAbilities extends AbstractMonsterAbilities {
    private int howlCooldown;
    private ServerWorld world;

    public WerwolfMonsterAbilities(AbstractMonsterType monsterType) {
        super(monsterType);
        this.howlCooldown = 0;
        if (monsterType.getProvider().getWorld() instanceof ServerWorld serverWorld) {
            this.world = serverWorld;
        }
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
        this.howl(world, this.getSelf().getBlockPos(), 200);
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
        if (key == 1) {
            this.howl(world, this.self.getBlockPos(), 100);
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.howlCooldown > 0) {
            this.howlCooldown--;
        }
        if (this.getSelf().getPitch() <= -80 && world.isNight()) {
            this.howl(world, this.getSelf().getBlockPos(), 200);
        }
    }

    private void howl(ServerWorld world, BlockPos pos, int newCooldown) {
        if (this.howlCooldown > 0) return;
        world.playSound(null, pos, NeMuelchSounds.WOLF_HOWL, SoundCategory.PLAYERS, 4f, 1f);
        this.howlCooldown = Math.max(this.howlCooldown, newCooldown);
    }
}
