package net.shirojr.nemuelch.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.compat.cca.implementation.OccasionsWorldComponent;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.monster.abilities.custom.MultiJumpAbility;
import net.shirojr.nemuelch.occasion.OccasionEntry;
import net.shirojr.nemuelch.util.duck.BoatDespawnHandler;
import net.shirojr.nemuelch.util.logger.LoggerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.OptionalDouble;

@Mixin(Entity.class)
public abstract class EntityMixin implements Nameable, EntityLike, CommandOutput {
    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V"))
    private void onSteppedOnAdditions(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if (!((Entity) (Object) this instanceof LivingEntity self)) return;
        if (!(self.getWorld() instanceof ServerWorld serverWorld)) return;
        MonsterComponent.get(self).getAbilities().onSteppedOn(serverWorld, self, movementType, movement);
    }

    /**
     * Implementation of Body Pull feature
     *
     * @param user Player who is about to pull the body
     * @param hand Hand, which is used to drag the body
     **/
    @Inject(method = "interactAt", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$interactAt(PlayerEntity user, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Entity entity = (Entity) (Object) this;
        ItemStack stack = user.getStackInHand(hand);

        if (!(entity instanceof ServerPlayerEntity targetPlayer)) return;
        if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) return;


        LoggerUtil.devLogger("not on cooldown");

        boolean isTool = stack.getItem() instanceof ShovelItem || stack.isIn(NeMuelchTags.Items.PULL_BODY_TOOLS);
        if (!isTool) return;
        if (!targetPlayer.isDead()) return;

        LoggerUtil.devLogger("targetPlayer is player and is dead");

        if (!user.getWorld().isClient()) {
            LoggerUtil.devLogger("applying operations on server side: " + getWorld());
            Vec3d pull = user.getPos().subtract(targetPlayer.getPos());
            pull.subtract(user.getRotationVector());

            targetPlayer.setVelocity(
                    pull.getX() * NeMuelchConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal(),
                    NeMuelchConfigInit.CONFIG.pullBodyFeature.getVelocity().getVertical(),
                    pull.getZ() * NeMuelchConfigInit.CONFIG.pullBodyFeature.getVelocity().getHorizontal()
            );
            targetPlayer.velocityModified = true;

            stack.damage(NeMuelchConfigInit.CONFIG.pullBodyFeature.getTool().getDamage(),
                    user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            user.getItemCooldownManager().set(stack.getItem(), NeMuelchConfigInit.CONFIG.pullBodyFeature.getTool().getCooldown());

            ServerWorld world = (ServerWorld) user.getWorld();
            world.playSound(null, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(),
                    SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.PLAYERS,
                    2f, 1f);
        }

        cir.setReturnValue(ActionResult.SUCCESS);
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V"))
    private void onSteppedOnBlight(Block instance, World world, BlockPos pos, BlockState state, Entity entity, Operation<Void> original) {
        original.call(instance, world, pos, state, entity);
        if (!(world instanceof ServerWorld serverWorld)) return;
        BlightChunkComponent.maybeGet(serverWorld.getChunk(entity.getChunkPos().x, entity.getChunkPos().z)).ifPresent(component -> {
            if (component.isEmpty()) return;
            world.getProfiler().push("nemuelch_on_stepped_on_blight");
            component.getBlightsOfPos(pos).forEach(type -> type.getActions().get().onSteppedOnBlock(
                    serverWorld, component.getTimeOfFirstInitializedBlight(), pos, entity
            ));
            world.getProfiler().pop();
        });

    }

    @Inject(method = "addPassenger", at = @At("TAIL"))
    private void onAddedPassenger(Entity passenger, CallbackInfo ci) {
        if (!(getWorld() instanceof ServerWorld)) return;
        Entity vehicleEntity = (Entity) (Object) this;
        if (!(vehicleEntity instanceof BoatDespawnHandler despawnHandler)) return;
        if (despawnHandler.isCountDownActive()) {
            despawnHandler.stopCountDown();
        }
    }

    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void onRemovedPassenger(CallbackInfo ci) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;
        Entity vehicleEntity = (Entity) (Object) this;
        if (!(vehicleEntity instanceof BoatDespawnHandler despawnHandler)) return;
        if (vehicleEntity.hasPassengers()) return;
        despawnHandler.neMuelch$setBoatEmptiedTime(serverWorld.getTime());
    }

    @WrapOperation(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void adjustSoundForOccasion(World instance, PlayerEntity except, double x, double y, double z, SoundEvent sound,
                                        SoundCategory category, float volume, float pitch, Operation<Void> original) {
        Entity entity = (Entity) (Object) this;
        OccasionsWorldComponent component = OccasionsWorldComponent.get(instance);
        float newPitch = pitch;
        float newVolume = volume;
        List<OccasionEntry> occasions = component.getUnsyncedActiveOccasions();
        for (OccasionEntry entry : occasions) {
            OptionalDouble occasionPitch = entry.getType().getEntitySoundPitch(entity, pitch);
            OptionalDouble occasionVolume = entry.getType().getEntitySoundVolume(entity, volume);
            if (occasionPitch.isPresent()) {
                newPitch = (float) occasionPitch.getAsDouble();
            }
            if (occasionVolume.isPresent()) {
                newVolume = (float) occasionVolume.getAsDouble();
            }
        }
        original.call(instance, except, x, y, z, sound, category, newVolume, newPitch);
    }

    @Inject(method = "onLanding", at = @At("HEAD"))
    private void resetMultiJump(CallbackInfo ci) {
        if (!((Entity) (Object) this instanceof LivingEntity livingEntity)) return;
        MonsterComponent.get(livingEntity).getAbilities().get(MultiJumpAbility.class).ifPresent(MultiJumpAbility::reset);
    }
}
