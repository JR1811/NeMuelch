package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.screen.handler.ParticleEmitterBlockScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ParticleEmitterBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    public static final String PARTICLE_ID_NBT_KEY = "heldParticle";
    private ParticleData currentParticle;


    public ParticleEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.PARTICLE_EMITTER, pos, state);
    }

    public void setCurrentParticleId(@Nullable Identifier currentParticleId) {
        if (this.currentParticle == null) {
            this.currentParticle = new ParticleData(currentParticleId, Vec3d.of(this.pos), new Vec3d(0, 0, 0), 1, 1);
        } else {
            this.currentParticle.setParticle(currentParticleId);
        }

        this.markDirty();
    }

    public @Nullable Identifier getCurrentParticleId() {
        if (this.currentParticle == null) return null;
        return this.currentParticle.getId();
    }

    public @Nullable ParticleData getCurrentParticle() {
        return this.currentParticle;
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

    public void setCurrentParticle(ParticleData data) {
        this.currentParticle = data;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (this.currentParticle != null) {
            nbt.put(PARTICLE_ID_NBT_KEY, this.currentParticle.toNbt());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(PARTICLE_ID_NBT_KEY)) {
            this.currentParticle = ParticleData.fromNbt(nbt.getCompound(PARTICLE_ID_NBT_KEY));
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ParticleEmitterBlockEntity particleEmitterBlockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (particleEmitterBlockEntity.currentParticle == null) return;
        Random random = world.random;

        if (random.nextInt(1, 10) <= 10) {
            ParticleEffect particle = getParticleFromIdentifier(particleEmitterBlockEntity.currentParticle.getId());
            Vec3d spawnPos = new Vec3d(
                    (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    (double) pos.getY() + random.nextDouble() + random.nextDouble(),
                    (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1));

            serverWorld.spawnParticles(particle, spawnPos.x, spawnPos.y, spawnPos.z, 1, 0.0, 0.078, 0.0, 1.0);
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.nemuelch.particle_emitter_block_gui_title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ParticleEmitterBlockScreenHandler(syncId, ScreenHandlerContext.create(this.getWorld(), this.getPos()), this.currentParticle, player);
    }


    /**
     * Data class to help out with particle handling of the ParticleEmitter Block and BlockEntity class
     */
    public static class ParticleData {
        public static final String PARTICLE_NBT_KEY = "particle", SPAWN_POS_NBT_KEY = "pos", DELTA_NBT_KEY = "delta";
        public static final String COUNT_NBT_KEY = "count", SPEED_NBT_KEY = "speed";

        private Identifier particleId;
        private Vec3d spawnPos, delta;
        private int count;
        private double speed;

        /**
         * @param particleId Identifier key for the particle type
         * @param spawnPos   spawn position of the particles
         * @param delta      delta vector of the particles
         * @param count      amount of spawned particles
         * @param speed      travel speed of spawned particles
         */
        public ParticleData(Identifier particleId, Vec3d spawnPos, Vec3d delta, int count, double speed) {
            this.particleId = particleId;
            this.spawnPos = spawnPos;
            this.delta = delta;
            this.count = count;
            this.speed = speed;
        }

        //region Accessor and Mutator
        public Identifier getId() {
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

        public static ParticleData fromNbt(NbtCompound nbt) {
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

        public static ParticleData getDefaultData() {
            return new ParticleData(new Identifier("campfire_smoke"), Vec3d.ZERO, new Vec3d(0, 0, 0), 1, 0.2);
        }
    }
}
