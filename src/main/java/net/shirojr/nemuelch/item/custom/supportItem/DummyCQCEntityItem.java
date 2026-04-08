package net.shirojr.nemuelch.item.custom.supportItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.shirojr.nemuelch.entity.custom.DummyCloseQuarterEntity;
import net.shirojr.nemuelch.init.NeMuelchEntities;

import java.util.function.Predicate;

public class DummyCQCEntityItem extends Item {
    public static final Predicate<BlockState> OBSTRUCTS_DUMMY_PLACEMENT = state -> !state.isAir() && !(state.getBlock() instanceof FluidBlock);

    public DummyCQCEntityItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (hand != Hand.MAIN_HAND || !(entity instanceof DummyCloseQuarterEntity dummyEntity)) {
            return super.useOnEntity(stack, user, entity, hand);
        }
        if (user.getWorld() instanceof ServerWorld) {
            dummyEntity.kill();
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        if (canPlaceDummy(world, blockPos)) {
            if (world instanceof ServerWorld serverWorld) {
                DummyCloseQuarterEntity spawnedEntity = NeMuelchEntities.DUMMY_CQC.spawn(
                        serverWorld, null, null, blockPos,
                        SpawnReason.SPAWN_EGG, true, false
                );
                if (spawnedEntity != null) {
                    if (player != null) {
                        if (!player.isCreative()) {
                            context.getStack().decrement(1);
                        }
                        spawnedEntity.setYaw(player.getHorizontalFacing().asRotation());
                    }
                    serverWorld.playSound(null, blockPos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.NEUTRAL);
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS;
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    private boolean canPlaceDummy(World world, BlockPos originalPos) {
        int height = Math.max(0, MathHelper.ceil(NeMuelchEntities.DUMMY_CQC.getHeight()));
        int width = Math.max(0, MathHelper.ceil(NeMuelchEntities.DUMMY_CQC.getWidth()));

        BlockPos startPos = originalPos.up();
        for (BlockPos.Mutable pos : BlockPos.iterateInSquare(startPos, width / 2, Direction.NORTH, Direction.EAST)) {
            for (int currentHeight = 0; currentHeight < height; currentHeight++) {
                BlockPos testPos = pos.up(currentHeight);
                if (OBSTRUCTS_DUMMY_PLACEMENT.test(world.getBlockState(testPos))) {
                    return false;
                }
            }
        }
        return true;
    }
}
