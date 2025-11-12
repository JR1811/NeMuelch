package net.shirojr.nemuelch.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.compat.cca.component.RottenMeatDigestionComponent;
import net.shirojr.nemuelch.init.NeMuelchBlocks;
import net.shirojr.nemuelch.item.util.ItemStackUtil;
import net.shirojr.nemuelch.util.helper.BlockPosHelper;

import java.util.*;
import java.util.function.BiPredicate;

@SuppressWarnings("deprecation")
public class RottenTreeLogBlock extends PillarBlock {
    public static final int MAX_LOG_SEARCH_COUNT = 70;
    public static final int MAX_LEAF_SEARCH_COUNT = 20;

    public RottenTreeLogBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!state.isOf(this) || !RottenMeatDigestionComponent.canDigest(stack))
            return super.onUse(state, world, pos, player, hand, hit);

        if (world.isClient()) return ActionResult.SUCCESS;

        HashSet<BlockPos> logs = collectValidConnected(world, Set.of(pos), (entryWorld, entryPos) -> {
            BlockState entryState = entryWorld.getBlockState(entryPos);
            return entryState.isOf(this);
        }, Set.of(), true, MAX_LOG_SEARCH_COUNT);

        HashSet<BlockPos> leaves = collectValidConnected(world, logs, (entryWorld, entryPos) -> {
            BlockState entryState = entryWorld.getBlockState(entryPos);
            BlockState entryStateBelow = entryWorld.getBlockState(entryPos.down());
            return entryState.isOf(NeMuelchBlocks.ROTTEN_MEAT) && entryStateBelow.isAir();
        }, Set.of(), false, MAX_LEAF_SEARCH_COUNT);

        if (leaves.isEmpty()) {
            player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.error"), true);
            return ActionResult.FAIL;
        }

        List<BlockPos> leavesList = new ArrayList<>();
        int highestAmount = 0;
        for (BlockPos leafPos : leaves) {
            Optional<RottenMeatDigestionComponent> component = RottenMeatDigestionComponent.get(world, leafPos);
            if (component.isPresent()) {
                if (component.get().isDigesting()) continue;
                int nonEmptyStacksAmount = component.get().getNonEmptyDigestionStackSize();
                if (nonEmptyStacksAmount >= RottenMeatDigestionComponent.MAX_DIGESTION_SIZE) continue;
                if (highestAmount < nonEmptyStacksAmount) {
                    highestAmount = nonEmptyStacksAmount;
                    leavesList.clear();
                }
            }
            leavesList.add(leafPos);
        }
        if (leavesList.isEmpty()) {
            player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.error"), true);
            return ActionResult.FAIL;
        }
        int randomLeafIndex = world.getRandom().nextInt(leavesList.size());
        BlockPos chosenLeafPos = leavesList.get(randomLeafIndex);
        BlockState chosenLeafState = world.getBlockState(chosenLeafPos);

        if (!chosenLeafState.contains(RottenMeatBlock.STAGE)) {
            player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.error"), true);
            return ActionResult.FAIL;
        } else if (chosenLeafState.get(RottenMeatBlock.STAGE) == 0) {
            RottenMeatBlock.jumpStartBlockEntity(world, chosenLeafPos, chosenLeafState, stack, true);
            player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.success"), true);
            return ActionResult.SUCCESS;
        } else {
            Optional<RottenMeatDigestionComponent> component = RottenMeatDigestionComponent.get(world, chosenLeafPos);
            if (component.isEmpty()) {
                player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.error"), true);
                return ActionResult.FAIL;
            }
            boolean success = component.get().addToDigestion(stack.copy(), true);
            if (success) {
                ItemStackUtil.decrementUnlessCreative(stack, player, stack.getCount());
                if (world instanceof ServerWorld serverWorld) {
                    RottenMeatBlock.spawnParticles(50, 1, chosenLeafPos.down(), serverWorld);
                }
                player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.success"), true);
                return ActionResult.SUCCESS;
            }
            player.sendMessage(Text.translatable("block.nemuelch.rotten_tree_log.search.error"), true);
            return ActionResult.FAIL;
        }
    }

    private static HashSet<BlockPos> collectValidConnected(World world, Collection<BlockPos> startPositions,
                                                           BiPredicate<World, BlockPos> isValid, Collection<BlockPos> excluded,
                                                           boolean checkSelf, int maxSize) {
        HashSet<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (BlockPos startPosEntry : startPositions) {
            if (excluded.contains(startPosEntry) || result.contains(startPosEntry)) continue;

            if (checkSelf) {
                if (!isValid.test(world, startPosEntry)) {
                    continue;
                }
                result.add(startPosEntry.toImmutable());
            }
            queue.add(startPosEntry.toImmutable());
        }

        while (!queue.isEmpty() && result.size() < maxSize) {
            BlockPos currentPos = queue.poll();
            for (BlockPos offset : BlockPosHelper.ALL_NEIGHBORS_CACHED) {
                BlockPos neighborPos = currentPos.add(offset);
                if (result.contains(neighborPos) || excluded.contains(neighborPos)) continue;
                if (isValid.test(world, neighborPos)) {
                    result.add(neighborPos.toImmutable());
                    queue.add(neighborPos.toImmutable());
                }
            }
        }

        return result;
    }
}
