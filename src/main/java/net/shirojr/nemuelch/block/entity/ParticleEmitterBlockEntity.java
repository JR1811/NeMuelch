package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class ParticleEmitterBlockEntity extends BlockEntity{
    @Nullable
    private Identifier currentParticle;


    public ParticleEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.PARTICLE_EMITTER, pos, state);

        this.currentParticle = Registry.PARTICLE_TYPE.getId(ParticleTypes.ASH);
    }

    public void setCurrentParticle(@Nullable Identifier currentParticle) {
        this.currentParticle = currentParticle;
        this.markDirty();
    }

    public @Nullable Identifier getCurrentParticle() {
        return this.currentParticle;
    }

    public static ParticleEffect getParticleFromIdentifier(Identifier id) {
        return (ParticleEffect) Registry.PARTICLE_TYPE.get(id);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (currentParticle != null) {
            nbt.putString("heldParticle", currentParticle.toString());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        var heldParticleId = nbt.getString("heldParticle");

        if (!heldParticleId.isEmpty()) {
            currentParticle = Identifier.tryParse(heldParticleId);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ParticleEmitterBlockEntity blockEntity) {
        if (!world.isClient()) return;
        Random random = world.random;

        if (random.nextInt(1, 10) <= 10) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof ParticleEmitterBlockEntity particleEmitterBlockEntity)) return;
            ParticleEffect particle = getParticleFromIdentifier(particleEmitterBlockEntity.currentParticle);
            world.addImportantParticle(particle, true,
                    (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    (double) pos.getY() + random.nextDouble() + random.nextDouble(),
                    (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                    0.0, 0.07, 0.0);
        }

        Stream<ParticleType<?>> test = Registry.PARTICLE_TYPE.stream().filter(particleType -> particleType == ParticleTypes.ASH);


    }


}
