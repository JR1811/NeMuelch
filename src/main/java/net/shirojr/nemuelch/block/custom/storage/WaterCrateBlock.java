package net.shirojr.nemuelch.block.custom.storage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.entity.custom.WaterCrateBlockEntity;
import net.shirojr.nemuelch.init.NeMuelchBlockEntities;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class WaterCrateBlock extends BlockWithEntity {
    public WaterCrateBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof WaterCrateBlockEntity blockEntity)) {
            return ActionResult.PASS;
        }
        ItemStack stack = player.getStackInHand(hand);
        boolean isFluidItem = FluidStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand)) != null;
        if (world.isClient()) {
            return isFluidItem ? ActionResult.SUCCESS : ActionResult.PASS;
        }
        if (FluidStorageUtil.interactWithFluidStorage(blockEntity.getFluidStorage(), player, hand)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
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
