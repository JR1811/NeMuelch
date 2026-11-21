package net.shirojr.nemuelch.effect.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.effect.util.StatusEffectLifecycle;

public class TrailStatusEffect extends StatusEffect implements StatusEffectLifecycle {
    public TrailStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEntityTick(ServerWorld world, LivingEntity entity, StatusEffectInstance instance) {
        StatusEffectLifecycle.super.onEntityTick(world, entity, instance);
        if (entity.fallDistance != 0) return;
        if (Math.abs(entity.horizontalSpeed - entity.prevHorizontalSpeed) <= 0) return;
        if (entity.age % 120 != 0) return;
        BlockPos posBelow = entity.getBlockPos().down();
        BlockState stateBelow = world.getBlockState(posBelow);

        if (!(stateBelow.getBlock() instanceof Fertilizable fertilizable)) return;
        if (!fertilizable.isFertilizable(world, posBelow, stateBelow, world.isClient())) return;
        fertilizable.grow(world, world.getRandom(), posBelow, stateBelow);
        world.playSound(null, posBelow, SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.5f, 1f);
    }
}
