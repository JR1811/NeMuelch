package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.helper.PullUpFeatureHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

public class UseEvents implements UseEntityCallback, UseBlockCallback {
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (world instanceof ServerWorld serverWorld) {
            UseEvents.applyBlightToBlock(serverWorld, hitResult, stack);
        }

        MonsterComponent monsterComponent = MonsterComponent.get(player);
        monsterComponent.getActiveType().ifPresent(type -> type.onInteractBlock(player, world, hand, hitResult));
        if (player.isSneaking()) {
            if (blockState.contains(CrateBlock.TYPE) && blockState.get(CrateBlock.TYPE) == CrateBlock.Type.SINGLE) {
                if (stack.isIn(NeMuelchTags.Items.CRATE_STANDS) && world.getBlockEntity(blockPos) instanceof CrateBlockEntity blockEntity) {
                    if (!blockEntity.hasStandStack()) {
                        blockEntity.setStandStack(stack.copyWithCount(1));
                        if (!player.isCreative()) {
                            stack.decrement(1);
                        }
                        CrateBlock.changeType(world, blockPos, CrateBlock.Type.ANGLED);
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        MonsterComponent monsterComponent = MonsterComponent.get(player);
        monsterComponent.getActiveType().ifPresent(type -> type.onInteractEntity(player, world, hand, entity, hitResult));

        ActionResult pullResult = pullUpOther(player, entity);
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

    private static ActionResult pullUpOther(PlayerEntity source, Entity target) {
        if (!PullUpFeatureHelper.canPullUp(source, target)) return ActionResult.PASS;
        PullUpFeatureHelper.applyPullUp(source, target);
        return ActionResult.SUCCESS;
    }
}
