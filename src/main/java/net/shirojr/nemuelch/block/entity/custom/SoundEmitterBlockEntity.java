package net.shirojr.nemuelch.block.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SoundEmitterBlockEntity extends BlockEntity {

    @Nullable
    private static Identifier currentSound;

    public SoundEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(NeMuelchBlockEntities.SOUND_EMITTER, pos, state);

        Set<Map.Entry<RegistryKey<ParticleType<?>>, ParticleType<?>>> setOfSounds =
                new HashSet<>(Registry.PARTICLE_TYPE.getEntrySet());
    }

    public void setCurrentParticle(@Nullable Identifier currentSound) {
        this.currentSound = currentSound;
    }

    public static Identifier getCurrentSoundId() {
        return currentSound;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (currentSound != null) {
            nbt.putString("heldSound", currentSound.toString());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        var heldSoundId = nbt.getString("heldSound");

        if (!heldSoundId.isEmpty()) {
            currentSound = Identifier.tryParse(heldSoundId);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, SoundEmitterBlockEntity be) {

        if (world.isClient()) return;

        Random random = world.random;
    }

}
