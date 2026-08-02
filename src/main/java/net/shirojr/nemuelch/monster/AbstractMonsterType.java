package net.shirojr.nemuelch.monster;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.shirojr.nemuelch.monster.abilities.util.AbilityRegistrar;
import net.shirojr.nemuelch.monster.abilities.util.MonsterTypeData;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public abstract class AbstractMonsterType implements MonsterTransitionCallback {
    public abstract void initAbilities(PlayerEntity player, AbilityRegistrar registrar, @Nullable MonsterTypeData data);

    @Nullable
    public MonsterTypeData createDynamicData(PlayerEntity player) {
        return null;
    }

    public void printExtraCommandInfo(ServerCommandSource source) {
    }

    @SuppressWarnings("SameParameterValue")
    protected void playSoundForProvider(LivingEntity entity, SoundEvent sound, SoundCategory category, Vec3d pos, float volume, float pitch) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) return;
        serverWorld.playSound(null, pos.x, pos.y, pos.z, sound, category, volume, pitch);
    }
}
