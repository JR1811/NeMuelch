package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ParticleEmitterBlockEntity extends BlockEntity {
    public static final String PARTICLE_ID_NBT_KEY = "heldParticle";

    @Nullable
    private Identifier currentParticleId;



    public ParticleEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.PARTICLE_EMITTER, pos, state);

        this.currentParticleId = Registry.PARTICLE_TYPE.getId(ParticleTypes.ASH);
    }

    public void setCurrentParticleId(@Nullable Identifier currentParticleId) {
        this.currentParticleId = currentParticleId;
        this.markDirty();
    }

    public @Nullable Identifier getCurrentParticleId() {
        return this.currentParticleId;
    }

    public static ParticleEffect getParticleFromIdentifier(Identifier id) {
        return (ParticleEffect) Registry.PARTICLE_TYPE.get(id);
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


}
