package net.shirojr.nemuelch.monster.abilities.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchSounds;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;
import net.shirojr.nemuelch.util.constants.NeMuelchNbtKeys;
import net.shirojr.nemuelch.util.helper.PlayerLookupUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DashAbility extends ActiveAbility {
    private static final int PARTICLE_SCAN_DEPTH = 2;

    private final int keybindIndex;
    private double strengthMultiplier;

    public DashAbility(int cooldown, int keybindIndex, double strengthMultiplier) {
        super(cooldown);
        this.keybindIndex = keybindIndex;
        this.setStrengthMultiplier(strengthMultiplier);
    }

    public void setStrengthMultiplier(double strengthMultiplier) {
        this.strengthMultiplier = Math.max(0, strengthMultiplier);
    }

    @Override
    public void keybindInteraction(int index, ServerPlayerEntity user, boolean pressed) {
        super.keybindInteraction(index, user, pressed);
        if (this.keybindIndex != index) return;
        if (this.isOnCooldown()) return;

        Vec3d lookVec = user.getRotationVec(1f);
        Vec3d dashVec = lookVec.multiply(0.6f, 0.3f, 0.6f).add(0, 0.3, 0).multiply(this.strengthMultiplier);
        user.addVelocity(dashVec);
        PlayerLookupUtil.trackingAndSelf(user).forEach(player -> player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user)));

        List<Vec3d> circleSectorFlatPoints = getCircleSectorFlatPoints(user, 1.6, 30, user.getRandom());
        this.summonParticles(user, circleSectorFlatPoints, 1.2);
        user.getServerWorld().playSound(null, user.getBlockPos(), NeMuelchSounds.SWOOSH, SoundCategory.PLAYERS, 1f, 0.8f);

        this.startCooldown();
    }

    @SuppressWarnings("SameParameterValue")
    private List<Vec3d> getCircleSectorFlatPoints(LivingEntity origin, double maxRadius, double sectorAngleInDeg, Random random) {
        double yawRad = Math.toRadians(origin.getYaw());
        double yawBehindRad = yawRad * Math.PI;
        double halfSectorAngleRad = Math.toRadians(sectorAngleInDeg / 2);

        List<Vec3d> points = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5) + 5; i++) {
            double randomAngleRad = yawBehindRad + (random.nextDouble() - 0.5) * 2 * halfSectorAngleRad;
            double uniformDistributedRadius = maxRadius * Math.sqrt(random.nextDouble());
            double x = -Math.sin(randomAngleRad) * uniformDistributedRadius;
            double z = Math.cos(randomAngleRad) * uniformDistributedRadius;
            points.add(new Vec3d(x, origin.getY(), z));
        }
        return points;
    }

    @SuppressWarnings("SameParameterValue")
    private void summonParticles(LivingEntity entity, List<Vec3d> points, double sprayStrength) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        for (Vec3d point : points) {
            GroundInformation contactPoint = getContactPoint(serverWorld, point);

            Vec3d originPos = entity.getPos();
            Vec3d sprayStart = contactPoint == null ? originPos : new Vec3d(originPos.x, contactPoint.pos.y, originPos.z);
            Vec3d sprayDirection = point.subtract(sprayStart);
            if (sprayDirection.lengthSquared() > 0.0001) {
                sprayDirection.normalize();
            }

            if (contactPoint == null) {
                serverWorld.spawnParticles(
                        ParticleTypes.POOF,
                        point.x, point.y, point.z,
                        0,
                        sprayDirection.x * sprayStrength,
                        sprayDirection.y * sprayStrength,
                        sprayDirection.z * sprayStrength,
                        1.0
                );
            } else {
                serverWorld.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, contactPoint.state),
                        point.x, point.y, point.z,
                        0,
                        sprayDirection.x * sprayStrength,
                        sprayDirection.y * sprayStrength,
                        sprayDirection.z * sprayStrength,
                        1.0
                );
            }
        }
    }

    @Nullable
    private GroundInformation getContactPoint(World world, Vec3d point) {
        int startY = (int) Math.floor(point.getY());
        BlockPos.Mutable walker = new BlockPos.Mutable(point.x, startY, point.z);
        for (int i = 0; i < DashAbility.PARTICLE_SCAN_DEPTH; i++) {
            int y = startY - 1;
            walker.set(point.x, y, point.z);
            BlockState blockState = world.getBlockState(walker);
            if (blockState.getCollisionShape(world, walker).isEmpty()) {
                continue;
            }
            Vec3d pos = new Vec3d(point.x, y + 1, point.z);
            return new GroundInformation(pos, blockState);
        }
        return null;
    }

    @Override
    public void fromNbt(NbtCompound nbt) {
        super.fromNbt(nbt);
        if (nbt.contains(NeMuelchNbtKeys.STRENGTH)) {
            this.strengthMultiplier = nbt.getDouble(NeMuelchNbtKeys.STRENGTH);
        }
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        super.toNbt(nbt);
        nbt.putDouble(NeMuelchNbtKeys.STRENGTH, this.strengthMultiplier);
    }

    private record GroundInformation(Vec3d pos, BlockState state) {
    }
}
