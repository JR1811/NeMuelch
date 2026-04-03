package net.shirojr.nemuelch.block.custom.storage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.WaterCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import net.shirojr.nemuelch.mixin.access.EntityBucketItemAccessor;
import net.shirojr.nemuelch.util.data.EntityStorageEntry;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class WaterCrateBlock extends BlockWithEntity {
    public WaterCrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) return super.onUse(state, world, pos, player, hand, hit);
        if (!(world.getBlockEntity(pos) instanceof WaterCrateBlockEntity blockEntity)) {
            return ActionResult.PASS;
        }
        ItemStack stack = player.getStackInHand(hand);
        Storage<FluidVariant> itemFluidStorage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand));

        if (!(world instanceof ServerWorld serverWorld)) {
            return itemFluidStorage != null || stack.getItem() instanceof EntityBucketItem ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        if (stack.getItem() instanceof EntityBucketItem bucketItem) {
            EntityType<?> entityType = ((EntityBucketItemAccessor) bucketItem).nemuelch$getEntityType();
            if (!blockEntity.hasEnoughFluidForEntityStorage()) {
                player.sendMessage(
                        Text.translatable("block.nemuelch.water_crate.error_not_enough_fluid",
                                blockEntity.getFluidStorage().amount / FluidConstants.BUCKET,
                                blockEntity.getMinFluidAmountForEntityStorage() / FluidConstants.BUCKET),
                        true
                );
                return ActionResult.FAIL;
            } else if (!blockEntity.hasFittingFluidVariantForEntityStorage(entityType)) {
                player.sendMessage(Text.translatable("block.nemuelch.water_crate.error_wrong_fluid"), true);
                return ActionResult.FAIL;
            }
            if (blockEntity.canStoreEntity(entityType)) {
                blockEntity.setStoredEntity(EntityStorageEntry.createFromBucketStack(stack, world));
                blockEntity.incrementFluid();
                serverWorld.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS);
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                player.getInventory().offerOrDrop(new ItemStack(Items.BUCKET));
                return ActionResult.SUCCESS;
            }
        } else if (blockEntity.getStoredEntity() != null && (stack.isOf(Items.BUCKET) || stack.isOf(Items.WATER_BUCKET))) {
            if (blockEntity.getStoredEntity().getEntity(world) instanceof Bucketable bucketable) {
                blockEntity.setStoredEntity(null);
                blockEntity.decrementFluid();
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                player.getInventory().offerOrDrop(bucketable.getBucketItem());
                serverWorld.playSound(null, pos, bucketable.getBucketFillSound(), SoundCategory.NEUTRAL);
                return ActionResult.SUCCESS;
            }
        }
        if (itemFluidStorage != null && FluidStorageUtil.interactWithFluidStorage(blockEntity.getFluidStorage(), player, hand)) {
            if (blockEntity.getFluidStorage().amount < blockEntity.getMinFluidAmountForEntityStorage()) {
                if (blockEntity.getStoredEntity() != null) {
                    blockEntity.releaseStoredEntity(pos.up().toCenterPos());
                }
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof WaterCrateBlockEntity blockEntity) {
            if (blockEntity.getFluidStorage().amount > 0 && !blockEntity.getFluidStorage().isResourceBlank()) {
                blockEntity.onBroken();
                Fluid fluid = blockEntity.getFluidStorage().getResource().getFluid();
                world.setBlockState(pos, fluid.getDefaultState().getBlockState(), NOTIFY_ALL);
                world.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WaterCrateBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, NeMuelchBlockEntities.WATER_CRATE, WaterCrateBlockEntity::tick);
    }
}
