package net.shirojr.nemuelch.event.handler;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.custom.storage.CrateBlock;
import net.shirojr.nemuelch.block.entity.custom.CrateBlockEntity;
import net.shirojr.nemuelch.compat.cca.component.BlightChunkComponent;
import net.shirojr.nemuelch.compat.cca.implementation.MonsterComponent;
import net.shirojr.nemuelch.compat.cca.util.BlightType;
import net.shirojr.nemuelch.init.NeMuelchConfigInit;
import net.shirojr.nemuelch.init.NeMuelchTags;
import net.shirojr.nemuelch.util.helper.PullUpFeatureHelper;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

public class UseEvents implements UseEntityCallback, UseBlockCallback {
    @SuppressWarnings("RedundantIfStatement")   // for future entries
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (world instanceof ServerWorld serverWorld) {
            UseEvents.applyBlightToBlock(serverWorld, hitResult, stack);
        }

        MonsterComponent monsterComponent = MonsterComponent.get(player);
        monsterComponent.getAbilities().onInteractBlock(player, world, hand, hitResult);
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
        if (hand != Hand.OFF_HAND) {
            ActionResult ladderPlacement = handleLadderPlacement(stack, world, player, hand, hitResult);
            if (ladderPlacement != ActionResult.PASS) {
                return ladderPlacement;
            }
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("RedundantIfStatement")   // for future entries
    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        MonsterComponent monsterComponent = MonsterComponent.get(player);
        monsterComponent.getAbilities().onInteractEntity(player, world, hand, entity, hitResult);

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

    private static ActionResult handleLadderPlacement(ItemStack stack, World world, PlayerEntity player, Hand hand,
                                                      BlockHitResult hitResult) {
        if (!NeMuelchConfigInit.CONFIG.enableLadderFeatures) return ActionResult.PASS;
        if (!(stack.getItem() instanceof BlockItem blockItem)) return ActionResult.PASS;
        if (player.isSneaking()) return ActionResult.PASS;
        Block block = blockItem.getBlock();
        if (!block.getDefaultState().isIn(NeMuelchTags.Blocks.CONVENTIONAL_LADDERS)) return ActionResult.PASS;
        BlockPos hitBlockPos = hitResult.getBlockPos();
        BlockState originalState = world.getBlockState(hitBlockPos);
        if (!originalState.isIn(NeMuelchTags.Blocks.CONVENTIONAL_LADDERS)) return ActionResult.PASS;

        boolean hitTopPart = (hitResult.getPos().getY() - hitBlockPos.getY()) >= 0.5;
        Direction offset = hitTopPart ? Direction.UP : Direction.DOWN;
        BlockPos.Mutable posWalker = hitBlockPos.mutableCopy().move(offset);
        int maxLadderSteps = 128;
        int steps = 0;
        while (steps <= maxLadderSteps && world.getBlockState(posWalker).isIn(NeMuelchTags.Blocks.CONVENTIONAL_LADDERS)) {
            posWalker.move(offset);
            steps++;
        }

        BlockHitResult newHitResult = new BlockHitResult(
                hitResult.getPos().offset(offset, 1),
                hitResult.getSide(),
                posWalker,
                true
        );
        ItemPlacementContext placementCtx = new ItemPlacementContext(player, hand, stack, newHitResult);
        if (!placementCtx.canPlace()) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.SUCCESS;
        ActionResult result = blockItem.place(placementCtx);
        if (result.isAccepted()) {
            BlockState placed = world.getBlockState(posWalker);
            if (placed.contains(LadderBlock.FACING) && originalState.contains(LadderBlock.FACING)) {
                world.setBlockState(posWalker, placed.with(LadderBlock.FACING, originalState.get(LadderBlock.FACING)));
                BlockSoundGroup soundGroup = placed.getSoundGroup();
                player.playSound(
                        soundGroup.getPlaceSound(), SoundCategory.BLOCKS,
                        (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F
                );
            }
        }
        return result.isAccepted() ? ActionResult.SUCCESS : ActionResult.PASS;
    }
}
