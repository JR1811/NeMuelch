package net.shirojr.nemuelch.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.*;

public class ParticleEmitterBlockEntity extends BlockEntity{

    @Nullable
    private static Identifier currentParticle;
    //private static List<ParticleEffect> pt = new ArrayList<>();
    //private static int particleListIndex = 0;


    public ParticleEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.PARTICLE_EMITTER, pos, state);

        Set<Map.Entry<RegistryKey<ParticleType<?>>, ParticleType<?>>> setOfParticles =
                new HashSet<>(Registry.PARTICLE_TYPE.getEntrySet());
    }

    public void setCurrentParticle(@Nullable Identifier currentParticle) {
        this.currentParticle = currentParticle;
    }

    public static Identifier getCurrentParticleId() {
        return currentParticle;
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

    public static void tick(World world, BlockPos pos, BlockState state, ParticleEmitterBlockEntity be) {

        if (world.isClient()) return;

        Random random = world.random;

        //world.addImportantParticle(pt.get(particleListIndex), true,
        /* world.addParticle(currentParticle);
                (double) pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                (double) pos.getY() + random.nextDouble() + random.nextDouble(),
                (double) pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double) (random.nextBoolean() ? 1 : -1),
                0.0, 0.07, 0.0);


--------------------------------------


        if (currentParticle != null) {

            Registry.PARTICLE_TYPE.get(currentParticle);
        }*/
    }

}
