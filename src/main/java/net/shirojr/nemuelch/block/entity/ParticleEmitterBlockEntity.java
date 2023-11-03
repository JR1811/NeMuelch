package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ParticleEmitterBlockEntity extends BlockEntity {
    public static final String PARTICLE_ID_NBT_KEY = "heldParticle";

    @Nullable
    private Identifier currentParticleId;


    public ParticleEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.PARTICLE_EMITTER, pos, state);

        //this.currentParticleId = Registry.PARTICLE_TYPE.getId(ParticleTypes.ASH);
    }

    public void setCurrentParticleId(@Nullable Identifier currentParticleId) {
        this.currentParticleId = currentParticleId;
        this.markDirty();
    }

    public @Nullable Identifier getCurrentParticleId() {
        return this.currentParticleId;
    }

    public static ParticleEffect getParticleFromIdentifier(Identifier id) {
        ParticleType<?> type = Registry.PARTICLE_TYPE.get(id);
        int rawId = Registry.PARTICLE_TYPE.getRawId(type);

        while (!(Registry.PARTICLE_TYPE.get(rawId) instanceof ParticleEffect)) {
            rawId++;
        }

        return (ParticleEffect) Registry.PARTICLE_TYPE.get(rawId);
        // return (ParticleEffect) Registry.PARTICLE_TYPE.get(id);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (currentParticleId != null) {
            nbt.putString(PARTICLE_ID_NBT_KEY, currentParticleId.toString());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        String heldParticleId = nbt.getString(PARTICLE_ID_NBT_KEY);
        if (!heldParticleId.isEmpty()) {
            currentParticleId = Identifier.tryParse(heldParticleId);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ParticleEmitterBlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        Random random = world.random;

        if (random.nextInt(1, 10) <= 10) {
            BlockEntity be = serverWorld.getBlockEntity(pos);
            if (!(be instanceof ParticleEmitterBlockEntity particleEmitterBlockEntity)) return;

            ParticleEffect particle = getParticleFromIdentifier(particleEmitterBlockEntity.currentParticleId);
            Vec3d spawnPos = new Vec3d(
                    (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    (double) pos.getY() + random.nextDouble() + random.nextDouble(),
                    (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1));

            serverWorld.spawnParticles(particle, spawnPos.x, spawnPos.y, spawnPos.z, 1, 0.0, 0.078, 0.0, 1.0);
        }

        Stream<ParticleType<?>> test = Registry.PARTICLE_TYPE.stream().filter(particleType -> particleType == ParticleTypes.ASH);


    }


    public static class ParticleData {
        public static final String PARTICLE_NBT_KEY = "particle", SPAWN_POS_NBT_KEY = "pos", DELTA_NBT_KEY = "delta";
        public static final String COUNT_NBT_KEY = "count", SPEED_NBT_KEY = "speed";

        private Identifier particleId;
        private Vec3d spawnPos, delta;
        private int count;
        private double speed;

        public ParticleData(Identifier particleId, Vec3d spawnPos, Vec3d delta, int count, double speed) {
            this.particleId = particleId;
            this.spawnPos = spawnPos;
            this.delta = delta;
            this.count = count;
            this.speed = speed;
        }

        //region Accessor and Mutator
        public Identifier getParticle() {
            return particleId;
        }

        public void setParticle(Identifier particle) {
            this.particleId = particle;
        }

        public Vec3d getSpawnPos() {
            return spawnPos;
        }

        public void setSpawnPos(Vec3d spawnPos) {
            this.spawnPos = spawnPos;
        }

        public Vec3d getDelta() {
            return delta;
        }

        public void setDelta(Vec3d delta) {
            this.delta = delta;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }
        //endregion

        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString(PARTICLE_NBT_KEY, particleId.toString());
            nbt.putInt(COUNT_NBT_KEY, count);
            nbt.putDouble(SPEED_NBT_KEY, speed);

            NbtCompound spawnPosCompound = new NbtCompound();
            spawnPosCompound.putDouble("X", spawnPos.x);
            spawnPosCompound.putDouble("Y", spawnPos.y);
            spawnPosCompound.putDouble("Z", spawnPos.z);
            nbt.put(SPAWN_POS_NBT_KEY, spawnPosCompound);
            NbtCompound deltaCompound = new NbtCompound();
            deltaCompound.putDouble("X", delta.x);
            deltaCompound.putDouble("Y", delta.y);
            deltaCompound.putDouble("Z", delta.z);
            nbt.put(DELTA_NBT_KEY, deltaCompound);

            return nbt;
        }

        public ParticleData fromNbt(NbtCompound nbt) {
            Identifier particleId = Identifier.tryParse(nbt.getString(PARTICLE_NBT_KEY));
            int count = nbt.getInt(COUNT_NBT_KEY);
            double speed = nbt.getDouble(SPEED_NBT_KEY);

            NbtCompound spawnPosCompound = (NbtCompound) nbt.get(SPAWN_POS_NBT_KEY);
            if (spawnPosCompound == null) {
                NeMuelch.devLogger("Couldn't parse particle spawnpos from NBT");
                return null;
            }
            Vec3d spawnPos = new Vec3d(
                    spawnPosCompound.getDouble("X"),
                    spawnPosCompound.getDouble("Y"),
                    spawnPosCompound.getDouble("Z"));

            NbtCompound deltaCompound = (NbtCompound) nbt.get(DELTA_NBT_KEY);
            if (deltaCompound == null) {
                NeMuelch.devLogger("Couldn't parse particle delta from NBT");
                return null;
            }
            Vec3d delta = new Vec3d(
                    deltaCompound.getDouble("X"),
                    deltaCompound.getDouble("Y"),
                    deltaCompound.getDouble("Z"));

            return new ParticleData(particleId, spawnPos, delta, count, speed);
        }
    }
}
