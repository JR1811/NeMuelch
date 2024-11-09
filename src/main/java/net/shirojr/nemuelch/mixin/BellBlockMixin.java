package net.shirojr.nemuelch.mixin;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.shirojr.nemuelch.init.ConfigInit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlock.class)
public abstract class BellBlockMixin extends BlockWithEntity {
    protected BellBlockMixin(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BellBlockEntity(pos, state);
    }

    @Inject(method = "ring(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void nemuelch$ringWithVariableDistance(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity bellEntity = world.getBlockEntity(pos);
        boolean defaultBellValues = ConfigInit.CONFIG.bellSound.getVolume() == 2.0F &&
                ConfigInit.CONFIG.bellSound.getPitch() == 1.0F;

        if (bellEntity instanceof BellBlockEntity && !world.isClient && !defaultBellValues) {
            if (direction == null) {
                direction = world.getBlockState(pos).get(BellBlock.FACING);
            }

            ((BellBlockEntity) bellEntity).activate(direction);
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS,
                    ConfigInit.CONFIG.bellSound.getVolume(), ConfigInit.CONFIG.bellSound.getPitch());
            world.emitGameEvent(entity, GameEvent.RING_BELL, pos);
            cir.setReturnValue(true);
        }
    }
}
