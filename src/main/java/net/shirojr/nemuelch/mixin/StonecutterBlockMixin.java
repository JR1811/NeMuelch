package net.shirojr.nemuelch.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.NeMuelch;
import net.shirojr.nemuelch.config.NeMuelchConfig;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;
import java.util.UUID;

@Mixin(StonecutterBlock.class)
public class StonecutterBlockMixin extends Block {
    @Shadow @Final public static DirectionProperty FACING;

    private boolean steppedOn;
    private UUID steppingEntityUuid = null;

    public StonecutterBlockMixin(Settings settings) {
        super(settings);
        this.steppedOn = false;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"))
    private static AbstractBlock.Settings nemuelch$tickingStonecutterBlock (AbstractBlock.Settings value) {
        return value.ticksRandomly();
    }


    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient() && ConfigInit.CONFIG.stoneCutterDamage) {
            if (entity instanceof PlayerEntity || entity instanceof ItemEntity) {
                this.steppedOn = true;
                this.steppingEntityUuid = entity.getUuid();
            }
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.steppedOn || this.steppingEntityUuid == null) return;

        Entity entity = world.getEntity(this.steppingEntityUuid);
        if (entity == null) return;

        Vec3d velocityInfluence = new Vec3d(0.45, 0.45, 0.45);
        Vec3d stonecutterInfluence;

        switch (state.get(FACING)) {
            case EAST -> stonecutterInfluence = new Vec3d(0.0, 1.0, -1.0);
            case SOUTH -> stonecutterInfluence = new Vec3d(1.0, 1.0, 0.0);
            case WEST -> stonecutterInfluence = new Vec3d(0.0, 1.0, 1.0);
            case NORTH -> stonecutterInfluence = new Vec3d(-1.0, 1.0, 0.0);

            default -> stonecutterInfluence = new Vec3d(0.0, 1.0, 0.0);
        }

        if (entity instanceof ServerPlayerEntity playerEntity) {
            Vec3d newVelocity = entity.getVelocity().add(stonecutterInfluence.multiply(velocityInfluence));
            playerEntity.setVelocity(newVelocity);
            playerEntity.velocityModified = true;

            playerEntity.damage(DamageSource.GENERIC, 6.0f);
            playerEntity.sendMessage(new TranslatableText("chat.nemuelch.standing_on_stonecutter"), true);
        }
        if (entity instanceof ItemEntity itemEntity) {
            itemEntity.damage(DamageSource.OUT_OF_WORLD, 4.0f);
        }

        this.steppedOn = false;
        this.steppingEntityUuid = null;

        super.randomTick(state, world, pos, random);
    }
}
