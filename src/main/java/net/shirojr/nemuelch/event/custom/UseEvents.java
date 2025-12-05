package net.shirojr.nemuelch.event.custom;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.component.GeneralMonsterComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.monster.AbstractMonsterType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

public class UseEvents implements UseEntityCallback, UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        if (world instanceof ServerWorld serverWorld) {
            UseEvents.applyBlightToBlock(serverWorld, hitResult, stack);
        }

        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            entry.getAbilities().onInteractBlock(player, world, hand, hitResult);
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        GeneralMonsterComponent monsterComponent = GeneralMonsterComponent.get(player);
        for (AbstractMonsterType entry : monsterComponent.getActiveMonsterTypes()) {
            ActionResult actionResult = entry.getAbilities().onInteractEntity(player, world, hand, entity, hitResult);
            if (actionResult != ActionResult.PASS) {
                return actionResult;
            }
        }
        ActionResult pullResult = pullUpOther(world, player, entity);
        if (pullResult != ActionResult.PASS) return pullResult;
        return ActionResult.PASS;
    }

    private static void applyBlightToBlock(ServerWorld serverWorld, BlockHitResult hitResult, ItemStack stack) {
        EnumSet<BlightType> blightTypes = BlightType.fromStack(stack);
        if (blightTypes.isEmpty() || hitResult.getType().equals(HitResult.Type.MISS)) return;
        BlockPos blockPos = hitResult.getBlockPos();
        Optional<BlightChunkComponent> blightChunkComponent = BlightChunkComponent.maybeGet(serverWorld.getChunk(blockPos));
        blightChunkComponent.ifPresent(chunkComponent -> chunkComponent.addBlightsToPos(blockPos, blightTypes));
    }

    private static ActionResult pullUpOther(World world, PlayerEntity source, Entity target) {
        if (source.isSneaking()) return ActionResult.PASS;
        if (target instanceof LivingEntity targetEntity) {
            if (targetEntity.isOnGround() || targetEntity.fallDistance > 0) return ActionResult.PASS;
            Vec3d pullForce = source.getPos().subtract(targetEntity.getPos()).multiply(0.2);
            pullForce = new Vec3d(pullForce.x, pullForce.y * 0.5, pullForce.z);
            targetEntity.addVelocity(pullForce);
            targetEntity.velocityModified = true;
            targetEntity.velocityDirty = true;
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
