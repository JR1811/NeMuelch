package net.shirojr.nemuelch.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shirojr.nemuelch.block.NeMuelchBlocks;
import net.shirojr.nemuelch.entity.custom.IgnitedScorchingTntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public abstract class TntBlockMixin extends Block {
    public TntBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "primeTnt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/LivingEntity;)V",
    at = @At("HEAD"), cancellable = true)
    private static void nemuelch$TntBlockTesting(World world, BlockPos pos, LivingEntity igniter, CallbackInfo info) {
        if (world.isClient) {
            info.cancel();
        }

        Entity tntEntity;

        //TODO: use custom block!
        if (world.getBlockState(pos.up()).getBlock() == NeMuelchBlocks.BLACK_FOG) {
            //TODO: implement own entity for custom block?
            tntEntity = new IgnitedScorchingTntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
        }
        else {
            tntEntity = new TntEntity(world, (double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, igniter);
        }


    }
}
