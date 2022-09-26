package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.shirojr.nemuelch.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean tick(BlockState instance, Block block) {

        return instance.isOf(block) || ConfigInit.CONFIG.beamIgnoresSolidBlocks;
    }
}
