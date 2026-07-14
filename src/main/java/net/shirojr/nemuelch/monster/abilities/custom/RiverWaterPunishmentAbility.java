package net.shirojr.nemuelch.monster.abilities.custom;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.shirojr.nemuelch.monster.abilities.ActiveAbility;

public class RiverWaterPunishmentAbility extends ActiveAbility {
    @Override
    public void tickServer(ServerPlayerEntity player) {
        super.tickServer(player);
        ServerWorld world = player.getServerWorld();
        if (world == null || player.age % 50 != 0) return;
        if (!world.getBiome(player.getBlockPos()).isIn(ConventionalBiomeTags.RIVER)) return;
        if (player.isTouchingWater()) {
            player.damage(world.getDamageSources().inFire(), 4f);
            world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.NEUTRAL, 1f, 0.85f);
        }
    }
}
