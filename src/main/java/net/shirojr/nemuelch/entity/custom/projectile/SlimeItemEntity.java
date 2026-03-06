package net.shirojr.nemuelch.entity.custom.projectile;

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
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.init.NeMuelchStatusEffects;
import net.shirojr.nemuelch.init.NeMuelchEntities;
import net.shirojr.nemuelch.item.custom.supportItem.SoapItem;

public class SlimeItemEntity extends ThrownItemEntity {
    public SlimeItemEntity(EntityType<SlimeItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public SlimeItemEntity(World world, LivingEntity owner) {
        super(NeMuelchEntities.SLIME_ITEM, owner, world);
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
                getWorld().addParticle(particleEffect, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
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

        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        if (!(targetEntity instanceof LivingEntity livingEntity)) return;
        BlockPos hitPos = livingEntity.getBlockPos();
        ItemStack firstSoapCoatedStack = SoapItem.getFirstCoatedStack(livingEntity);
        if (firstSoapCoatedStack != null) {
            SoapItem.decrementCoating(firstSoapCoatedStack);
            serverWorld.playSound(null, hitPos.up(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.NEUTRAL, 1f, 1f);
            return;
        }

        if (livingEntity instanceof PlayerEntity player) {
            if (player.isBlocking()) {
                ItemStack blockingStack = player.getMainHandStack();
                player.getItemCooldownManager().set(blockingStack.getItem(), 100);
            } else {
                livingEntity.addStatusEffect(new StatusEffectInstance(NeMuelchStatusEffects.SLIMED, 80, 0, false, true, true));
            }
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
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        serverWorld.spawnParticles(ParticleTypes.ITEM_SLIME, hitPos.getX() + 0.5, hitPos.getY() + 1, hitPos.getZ() + 0.5,
                10, 0.5, 0, 0.5, 0.7);
        serverWorld.playSound(null, hitPos.up(), SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.NEUTRAL,
                0.5f, 1.0f);

        this.discard();
    }
}
