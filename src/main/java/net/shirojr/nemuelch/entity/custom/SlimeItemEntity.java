package net.shirojr.nemuelch.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.effect.NeMuelchEffects;
import net.shirojr.nemuelch.entity.NeMuelchEntities;

public class SlimeItemEntity extends ThrownItemEntity {
    public SlimeItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SlimeItemEntity(World world, LivingEntity owner) {
        super(NeMuelchEntities.SLIME_ITEM, owner, world);
    }

    public SlimeItemEntity(World world, double x, double y, double z) {
        super(NeMuelchEntities.SLIME_ITEM, x, y, z, world);
    }


    @Override
    protected Item getDefaultItem() {
        return Items.SLIME_BALL;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 3) {
            ParticleEffect particleEffect = this.getParticleParameters();
            for (int i = 0; i < 8; ++i) {
                this.world.addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private ParticleEffect getParticleParameters() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? ParticleTypes.ITEM_SLIME : new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity targetEntity = entityHitResult.getEntity();

        if (!(this.world instanceof ServerWorld serverWorld)) return;
        if (!(targetEntity instanceof LivingEntity livingEntity)) return;
        BlockPos hitPos = livingEntity.getBlockPos();

        if (livingEntity instanceof PlayerEntity) {
            livingEntity.addStatusEffect(new StatusEffectInstance(NeMuelchEffects.SLIMED, 80, 0, false, true, true));
        } else {
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 3, false, true, true));
        }



        serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5,
                10, 0.5, 0, 0.5, 0.7);
        serverWorld.playSound(null, hitPos.up(), SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.NEUTRAL,
                0.5f, 1.0f);
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        BlockPos hitPos = blockHitResult.getBlockPos();
        if (!(this.world instanceof ServerWorld serverWorld)) return;

        serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5,
                10, 0.5, 0, 0.5, 0.7);
        serverWorld.playSound(null, hitPos.up(), SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.NEUTRAL,
                0.5f, 1.0f);

        this.discard();
    }
}
